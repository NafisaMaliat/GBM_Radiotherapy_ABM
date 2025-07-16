#!/bin/bash

# Set the classpath to point to your compiled classes
CLASS_PATH="/Users/tanayabowade/Downloads/ABM_GliobMul/out/production/HALModeling2024"

SCENARIOS=("Control" "BB5" "BB10" "BB15" "MRT200" "MRT400" "MRT600" "MB180" "MB350" \
           "Pred_MRT180" "Pred_MRT350" "Pred_MB200" "Pred_MB400" "Pred_MB600")

TRIALS=10

for SCENARIO in "${SCENARIOS[@]}"
do
  for (( i=1; i<=TRIALS; i++ ))
  do
    echo "▶️ Running scenario $SCENARIO | Trial $i"
    java -cp "$CLASS_PATH" OnLattice2DCells.Main "$SCENARIO"
    echo "✅ Finished scenario $SCENARIO | Trial $i"
    echo ""
  done
done

echo "🎉 All simulations complete!"
