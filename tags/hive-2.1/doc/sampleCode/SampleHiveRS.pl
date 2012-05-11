#!/usr/bin/perl

# Simple demonstration of calling a hive-rs function in Perl.

use LWP::Simple;

$url = get 'http://hive.nescent.org/hive-rs/schemes/nbii/concepts/prefLabels/a';

print $url;


