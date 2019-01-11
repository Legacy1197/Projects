import pandas as pd 
import numpy as np 
import json


section_stat = {}

with open('guardianMetaData.jsonl') as f:
    for i, line in enumerate(f):
        meta = json.loads(line) 
        section = meta['sectionId']
        if section not in section_stat:
            section_stat[section] = []
        section_stat[section].append(i)
    
for label, count in sorted([(k, len(v)) for k, v in section_stat.items()], key=lambda x: x[1], reverse=True):
    print("Section: %s -- Count: %s" % (label, count))

data = pd.read_csv('Guardian.csv').values
print(data.shape)
label_stat = {}

for i, label in enumerate(data[:, -1]):
    if label not in label_stat:
        label_stat[label] = []
    label_stat[label].append(i)

for label, count in sorted([(k, len(v)) for k, v in label_stat.items()], key=lambda x: x[1], reverse=True):
    print("label: %s -- count: %s" % (label, count))