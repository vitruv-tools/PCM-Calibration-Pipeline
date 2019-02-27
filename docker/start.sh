#!/bin/bash
set -e

echo "Start"
# updating
cd /etc/pipeline/PCM-Calibration-Pipeline/
git pull
cd /etc/pipeline/PCM-Calibration-Pipeline/modelrefinement.parameters.root/
gradle bootJar

# running
cd /etc/pipeline/PCM-Calibration-Pipeline/modelrefinement.parameters.root/modelrefinement.parameters.interface/build/libs/

java -jar modelrefinement-interface-0.1.0.jar