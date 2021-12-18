import os
import pandas as pd


# Method is wholly dependent on the variables
def init_var_dict():
	var_dict = {
	"Date":         	True, #Needs to be always accessible
	"Race_Num":     	True, #Needs to be always accessible
	"Track_Type":   	False,
	"Weather":      	False,
	"Dist":         	True,
	"Condition":    	False,
	"Track_Record":  	False,
	"Final_Time":    	False,
	"Last_Race":    	False,
	"Past_Race":		False,
	"Past_Track":		False,
	"Past_Pos": 	 	False,
	"Pgm":				False,
	"H_Name":   		True,
	"J_Name":			True,
	"Wgt":		 		False,
	"M/E":			 	False,
	"PP":			 	False,
	"Fin":				True, #Needs to be always accessible
	"Dist_Ahead":		False,
	"Odds":			 	True,
	"Fav":				True,
	"Comments":  		False
	}
	return var_dict;

def return_vars(cols, dict):
	eval_cols = [];
	print(cols)

	for i in range(len(cols)-1):
		if(dict[cols[i]]):
			eval_cols.append(cols[i]);

	return eval_cols;

def fin_trifecta(dataframe):
	dataframe

if __name__ == "__main__":
	data_dir = (os.path.dirname(os.path.abspath(__file__))).split('3. Python ML Predictions')[0].replace("\\", "/")+ '2. Java PDF-Data Conditioner/PHRA/Data.csv';
	df = pd.read_csv(data_dir);

	eval_cols = return_vars(df.columns, init_var_dict())
	df = pd.read_csv(data_dir, usecols = eval_cols)
	print(df.head)

	


	#Writes Conditioned Data back to another CSV file to be easily piped into ML evaluation
	df.to_csv("DATA_ML.csv", encoding='utf-8', index=False)
