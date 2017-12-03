#!/usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt

data = np.genfromtxt('..\..\Scheduler_Scarborough.log',skip_header=1,dtype=None)

clockData = data['f0']
names = data['f1']
locationData = data['f2']

# alter the location numbers above 12 to count back down
indices = np.where(locationData>12)
locationData[indices] = 24-locationData[indices]

# Convert the names to strings
names = np.array([n.decode('utf-8') for n in names])
uniqueNames = np.unique(names)

# For each of the configurations, get the indices
# associated with each of the unique train ids.
# Pull out the clock times and locations for each
# unique id.
clocks = {}
locations = {}

for name in uniqueNames:
    
    indices = np.where(names==name)
    clocks[name] = clockData[indices]
    locations[name] = locationData[indices]

plt.figure()
for name in uniqueNames:
    plt.plot(clocks[name],locations[name])
    plt.grid(True)
    plt.xlabel('Time (minutes)')

plt.show()