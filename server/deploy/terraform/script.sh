#!/bin/bash

IP=$1
echo "IP is $1"

sudo apt-get update
sudo apt-get install openjdk-8-jdk
#scp /Users/david/development/repos/elasticsearch-5.3.0-cloud.zip root@$IP:/tmp
mkdir -p /opt/moneycol
#mv /tmp/elasticsearch-5.3.0-cloud /opt/moneycol
sudo apt-get install zip

adduser moneycol
usermod -aG sudo moneycol
su moneycol
sudo chown -R moneycol:moneycol /opt/moneycol

# https://www.digitalocean.com/community/tutorials/ufw-essentials-common-firewall-rules-and-commands
sudo ufw enable
sudo ufw allow 9200
sudo sysctl -w vm.max_map_count=262144

echo "End"

# jvm.options => 1500M
# elasticsearch.yml => network.host = 0.0.0.0 
# elasticsearch.yml => path.repo commented out