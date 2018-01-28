#!/usr/bin/perl

use warnings;
use strict;
use Time::HiRes qw(usleep);

select STDOUT; $| = 1;

while() {
	# PLAY

	sleep(10);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  2B 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  2B 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	sleep(5);

	# NEXT
	sleep(1);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  02 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  02 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	sleep(5);

	# PREV
	sleep(1);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	sleep(5);

	# NEXT LONG
	sleep(5);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(300000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  03 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(100000);
	print("  can0  5C1   [4]  00 00 00 60\n");
	usleep(300000);
	

	sleep(10);
}
