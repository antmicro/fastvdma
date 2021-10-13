#!/bin/bash
sudo apt-get --allow-releaseinfo-change update && sudo apt-get install -y --no-install-recommends apt-transport-https curl gnupg graphicsmagick-imagemagick-compat graphicsmagick-imagemagick-compat default-jdk default-jre
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list:wq
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update && sudo apt-get install -y sbt
