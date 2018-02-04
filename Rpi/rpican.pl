#!/usr/bin/perl

use strict;
use warnings;
use threads;
use threads::shared;

use Net::Bluetooth;
use Time::HiRes qw(usleep);
use Thread::Queue;
use IO::Handle;
use IO::Select;
use Symbol qw(qualify_to_ref);

my $CANDUMP = '/usr/bin/candump';
my $BT = 'BT';
my $CAN = 'CAN';

my $server;
my $cl;
my $service;
my $connected :shared;

# TODO: PID file management for single process execution

my $btRecv 	= Thread::Queue->new();
my $btSend 	= Thread::Queue->new();

my $logEnabled 	= $ARGV[0];
my $logFile		= $ARGV[1];

if (defined($logEnabled) and $logEnabled =~ /-\w*l\w*/ and !$logFile) {
	die("Usage: rpican.pl <-l | -p | -lp> [/path/to/log/file]\n-l - log to file only\n-p - log to STDOUT only\n-lp - log to both\n");
}
if ($logFile) {
	open(LOGFILE, ">>".$logFile) or die("Cannot open log file for writing.\n");
	select((select(LOGFILE), $|=1)[0]);
}

$SIG{'INT'} = \&cleanup;
$SIG{'TERM'} = \&cleanup;
$SIG{'KILL'} = \&cleanup;

$connected = 0;
select((select(STDOUT), $|=1)[0]);

logger("Main thread started. PID: ".$$);

my $bt 	= threads->create(\&bluetooth);
my $can = threads->create(\&can);

while () {
	my $recv = $btRecv->dequeue();
	if ($recv eq "-2") {
		logger("Android responded with 'Unknown command'.");
	}
	elsif ($recv ne "0") {
		logger("Error BT response received. Restarting BT service...");
		$bt->kill("KILL")->detach();
		sleep(5);
		$bt = threads->create(\&bluetooth);
	}
}

sub can {
	$SIG{"KILL"} = sub {
		logger("Silence!!! I kill you CAN...", $CAN);
		close(CAN_FH);
		threads->exit();
	};

	logger("CAN thread started.", $CAN);

	my $lastBtn = 0x00;

	# if (!open(CAN_FH, "cansim.pl |")) {
	if (!open(CAN_FH, $CANDUMP." can0,5c1:FFFFFFFF |")) {
		 logger("Cannot start candump tool '".$!."'.", $CAN);
		 cleanup(1);
	}
	while(<CAN_FH>) {
		next if (!$connected);

		my $row = $_;
		chomp($row);
		
		my $btn = hex(substr($row, 19, 2));
		next if ($btn == $lastBtn);

		if ($btn == 0x02) {
			$btSend->enqueue("NEXT_DOWN");
		}
		elsif ($btn == 0x03) {
			$btSend->enqueue("PREV_DOWN");
		}
		elsif ($btn == 0x2B) {
			$btSend->enqueue("PLAY_PAUSE_DOWN");
		}
		elsif ($btn == 0x00) {
			if ($lastBtn == 0x02) {
				$btSend->enqueue("NEXT_UP");
			}
			elsif ($lastBtn == 0x03) {
				$btSend->enqueue("PREV_UP");
			}
			elsif ($lastBtn == 0x2B) {
				$btSend->enqueue("PLAY_PAUSE_UP");
			}
		}

		$lastBtn = $btn;
	}
	close(CAN_FH);
}

sub bluetooth {
	$SIG{"KILL"} = sub {
		logger("Silence!!! I kill you BT...", $BT);
		$connected = 0;
		close($cl);
		$service->stopservice();
		$server->close();
		threads->exit();
	};

	logger("BT thread started.", $BT);

	$server = Net::Bluetooth->newsocket("RFCOMM");

	if ($server->bind(7)) {
		logger("FATAL: Unable to bind '".$!."'. Exiting...", $BT);
		cleanup(1);
	}

	if ($server->listen(2)) {
		logger("FATAL: Unable to start to listen '".$!."'. Exiting...", $BT);
		cleanup(1);
	}

	$service = Net::Bluetooth->newservice($server, "00001101-0000-1000-8000-00805F9B34FB", "CanBusIF", "CanBus interface on Raspberry Pi2");
	if (!defined($service)) {
		logger("FATAL: Cannot register service '".$!."'. Exiting...", $BT);
		cleanup(1);
	}

	logger("BT service started. Waiting for connection...", $BT);

	my $client = $server->accept();
	if (!defined($client)) {
		logger("FATAL: Client accept() failed '".$!."'. Exiting...", $BT);
		cleanup(1);
	}

	my ($clientAddress, $port) = $client->getpeername();

	logger("Client '".$clientAddress."' on channel '".$port."' connected.", $BT);

	$connected = 1;

	$cl = $client->perlfh();

	my $buffer;

	# Making the CLIENT filehandle "hot", i.e. unbuffered
	select((select($cl), $|=1)[0]);
	
	while () {
		my $command = $btSend->dequeue();

		logger("Sending '".$command."' command...", $BT);

		print $cl $command,"\n";
		$buffer = sysreadline($cl, 3);
		chomp($buffer);

		logger("Received response: '".$buffer."'", $BT);		

		if (! defined($buffer)) {
			$btRecv->enqueue("-1");
		}
		else {
			$btRecv->enqueue($buffer);
		}
	}
}

sub sysreadline(*;$) {
    my($handle, $timeout) = @_;
    $handle = qualify_to_ref($handle, caller( ));
    my $infinitely_patient = (@_ == 1 || $timeout < 0);
    my $start_time = time( );
    my $selector = IO::Select->new( );
    $selector->add($handle);
    my $line = "";
SLEEP:
    until (at_eol($line)) {
        unless ($infinitely_patient) {
            return $line if time( ) > ($start_time + $timeout);
        }
        # sleep only 1 second before checking again
        next SLEEP unless $selector->can_read(1.0);
INPUT_READY:
        while ($selector->can_read(0.0)) {
            my $was_blocking = $handle->blocking(0);
CHAR:       while (sysread($handle, my $nextbyte, 1)) {
                $line .= $nextbyte;
                last CHAR if $nextbyte eq "\n";
            }
            $handle->blocking($was_blocking);
            # if incomplete line, keep trying
            next SLEEP unless at_eol($line);
            last INPUT_READY;
        }
    }
    return $line;
}
sub at_eol($) { $_[0] =~ /\n\z/ }

sub logger {
	my $msg = shift;
	my $tag = shift;

	return if (!$logEnabled);

	$msg = scalar(localtime())." (".threads->tid().") <".( $tag ? $tag : "MAIN" )."> ".$msg."\n";

	if ($logEnabled =~ /-\w*p\w*/) {
		print $msg;
	}
	if ($logEnabled =~ /-\w*l\w*/) {
		print LOGFILE $msg;
	}
}

sub cleanup {
	my $restart = shift;

	logger("Termination signal caught. Cleanup and exit...");
	
	if (defined($bt) and !$bt->is_detached()) {
		$bt->kill("KILL")->detach();
	}
	if (defined($can) and !$can->is_detached()) {
		$can->kill("KILL")->detach();
	}

	if ($logEnabled =~ /-\w*l\w*/) {
		close(LOGFILE);
	}

	if (!$restart) {
		exit(0);
	}
	else {
		# sleep(5);
		# exec($^X, $0, @ARGV);
		system("/sbin/reboot &");
		exit(-1);
	}
}
