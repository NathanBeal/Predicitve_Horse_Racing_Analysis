import os
import pandas as pd

data_dir = (os.path.dirname(os.path.abspath(__file__))).split('3. Python ML Predictions')[0].replace("\\", "/")+ '2. Java PDF-Data Conditioner/PHRA/Data.csv';

df = pd.read_csv(data_dir)
print(df)