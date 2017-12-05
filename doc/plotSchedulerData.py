#!/usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter, MaxNLocator

xs = [0,2,4,6,8,10]
labels = {0:'Kennedy',2:'Lawrence East',4:'Ellesmere',6:'Midland',8:'Scarborough Centre',10:'McCowan'}

def format_yticks(tick_val,tick_pos):
    if int(tick_val) in xs:
        return labels[int(tick_val)]
    else: 
        return ''

def plotSchedule(filename, data):
    clockData = data['f0']
    names = data['f1']
    locationData = data['f2']

    # alter the location numbers above 12 to count back down
    indices = np.where(locationData>10)
    locationData[indices] = 22-locationData[indices]

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

    fig = plt.figure()
    for name in uniqueNames:
        fig.plot(clocks[name],locations[name])
        fig.grid(True)
        fig.xlabel('Time (minutes)')

    fig.gca().yaxis.set_major_formatter(FuncFormatter(format_yticks))
    fig.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
    fig.savefig('{}.png'.format(filename))
    fig.show()