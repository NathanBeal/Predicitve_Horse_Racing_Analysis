import os
import pandas as pd

print(os.path.dirname(os.path.abspath(__file__)))
curr_dir = os.path.dirname(os.path.abspath(__file__))
curr_dir = curr_dir.split('3. Python ML Predictions')[0];
curr_dir = curr_dir.replace("\\", "/")
curr_dir += '2. Java PDF-Data Conditioner/PHRA/Data.csv';
print(curr_dir)

df = pd.read_csv(curr_dir, skiprows = 1)
print(df)
