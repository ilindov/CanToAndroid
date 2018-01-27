use strict;
use warnings;
use diagnostics;
use threads;

use Net::Bluetooth;
use Time::HiRes qw(usleep);
use Thread::Queue;

my $server;
my $cl;
my $service;

my $btCommRecv = Thread::Queue->new();
my $btCommSend = Thread::Queue->new();

my $bt = threads->create(\&bluetooth);
print "Main thread: ", threads->tid(), "\n";
print "BT thread started: ", $bt->tid(), "\n";

while () {
	my $recv = $btCommRecv->dequeue();
	print "'", $recv, "'\n";
	if ($recv ne "0") {
		$bt->kill("KILL")->detach();
		sleep(5);
		$bt = threads->create(\&bluetooth);
		print "BT thread started: ", $bt->tid(), "\n";
	}
}

sub bluetooth {

	$SIG{"KILL"} = sub {
		print "Silence!!! I kill you... (", threads->tid(), ")\n";
		close($cl);
		$service->stopservice();
		$server->close();
		threads->exit();
	};

	$server = Net::Bluetooth->newsocket("RFCOMM");

	if ($server->bind(7)) {
		die "FATAL: Unable to bind. Exiting...\n";
	}

	if ($server->listen(2)) {
		die "FATAL: Unable to start to listen. Exiting...\n";
	}

	$service = Net::Bluetooth->newservice($server, "00001101-0000-1000-8000-00805F9B34FB", "CanBusIF", "CanBus interface on Raspberry Pi2");
	if (!defined($service)) {
		die "FATAL: Cannot register service. Exiting...\n";
	}

	print "Service started. Waiting for connection...\n";

	my $client = $server->accept();
	if (!defined($client)) {
		die "FATAL: Client accept() failed. Exiting...\n";
	}

	my ($clientAddress, $port) = $client->getpeername();

	print "Client '", $clientAddress, "' on channel '", $port,"' connected.\n";

	$cl = $client->perlfh();

	my $buffer;

	# Making the CLIENT filehandle "hot", i.e. unbuffered
	select((select($cl), $|=1)[0]);
	select((select(STDOUT), $|=1)[0]);

	while () {
		print $cl "0\n";
		$buffer = sysreadline($cl, 1);
		chomp($buffer);

		if (! defined($buffer)) {
			$btCommRecv->enqueue("-1");
		}
		else {
			$btCommRecv->enqueue($buffer);
		}

		usleep(300000);
	}
}



use IO::Handle;
use IO::Select;
use Symbol qw(qualify_to_ref);

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
