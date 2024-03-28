#!/bin/bash

apt-get update
apt-get install openjdk-8-jdk -y


###TODO#########
'''
# Define function to join array elements with a delimiter
join() { local IFS="$1"; shift; echo "$*"; }

# Use the IPs of bootstrap nodes passed as parameters
bootstrap_ips=("$@")
bootstrap_ips_str=$(join "," "${bootstrap_ips[@]}")

echo "Bootstrap Node IPs: $bootstrap_ips_str"
'''