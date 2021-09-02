import PyPDF2, csv, numpy as np
from Parsing_Methods import *

def main():
	pdf = open('3-15-20 AQU.pdf', 'rb')
	pdf_reader = PyPDF2.PdfFileReader(pdf)
	print('Number of Pages: ', pdf_reader.numPages)

	racing_data = [];
	init_csv(list_of_variables);
	for i in range(0,9):
		racing_data = read_race_data(pdf_reader.getPage(i).extractText().split())
		write_to_csv(racing_data);


# Receives data for a specific race and organizes 
def read_race_data(raw_data):

	try: date, race_number = record_date_race_num(raw_data)
	except Exception as e: print('ERROR: record_date_race_num', e)
	try: racing_data = record_racing_data(raw_data, diagnosis = False)
	except Exception as e: print('ERROR: racing_data', e)

	print('Date: ' + date)
	print('Race Number: ' + race_number)
	for i in range(len(racing_data)):
		print(racing_data[i])
	print(100*'-')

	return racing_data;

def record_racing_data(lines, diagnosis):
	try: top_bound = lines.index('Replay') + 2;
	except Exception as e: print(35*'#');print('ERROR: Issue finding top_bound', e);print(35*'#');

	try: bottom_bound = lines.index('FractionalTimes:');
	except Exception as e: print(35*'#');print('ERROR: Issue finding bottom_bound', e);print(35*'#');

	# Determine number of horses
	num_of_horses = 0;
	indexes = [top_bound];
	for i in range(top_bound, bottom_bound):
		if(len(list(lines[i])) > 7):
			for j in range(len(list(lines[i]))):
				if(list(lines[i])[j] == '.'):
					num_of_horses += 1;
					indexes.append(i+1);

	if(num_of_horses == 0): print(35*'#');print('ERROR: Issue finding horses');print(35*'#');

	# Determines what parameters are written to Excel
	parameters_list = [];
	for i in range(len(list_of_variables)):
		if(RACING_PARAMETERS(list_of_variables[i])): parameters_list.append(list_of_variables[i]);

	# Populates Horse Array based on indexes found earlier
	race_array = [parameters_list]
	for i in range(len(indexes)-1):
		race_array.append(lines[indexes[i]:indexes[i+1]])

	# 
	try: organized_race_array = organize_race_array(race_array, diagnosis);
	except Exception as e: print(35*'#');print('ERROR:--', e); print(35*'#');

	return(organized_race_array)

def organize_race_array(race_array, diagnosis = False):
	O_ARR = [];
	O_ARR.append(list_of_variables)
	try:
		for i in range(1,len(race_array)):
			# INITS
			edited_array = [];
			horse_name = jockey_name = jockey_weight = med = equip = pole_position = '';
			odds = comments = ''; favorite = 1;

			race_array = catch_line_reading_isssues(race_array);
			# HORSE'S FIRST RACE
			if(list(race_array[i][0])[0] == '-'): #first race data
				#print('ALL LINE DATA: ', race_array[i]) #MUTE HERE

				race_array[i], char_horse_index = organize_line_data(race_array[i], FIRST_RACE = True)
				return_arr = parse_racer_data(race_array[i], char_horse_index, race_array[i].pop());
				O_ARR.append(['-', '-', '-', '-'] + [i] + return_arr)

			else: #NOT HORSE'S FIRST RACE
				race_array[i], char_horse_index = organize_line_data(race_array[i])
				return_arr = parse_racer_data(race_array[i], char_horse_index, race_array[i].pop());
				O_ARR.append([race_array[i][0], race_array[i][1], race_array[i][2], race_array[i][3]] + [i] + return_arr)


	except Exception as e: print('Edits made with bad line read', e)


	return O_ARR;

#
# POLE POSITION, HORSE NAME, JOCKEY NAME, WEIGHT, MEDICINE, EQUIPMENT
#
def parse_racer_data(data_arr, char_horse_index, data_str):
	#print('data_arr: ', data_arr) #MUTE HERE
	# Inits
	horse_name = jockey_name = jockey_weight = med = equip = pole_position = '';
	odds = comments = ''; favorite = 0;

	# Extract Pole Position
	try: pole_position = data_arr[char_horse_index-1];
	except Exception as e: print('pole_position', e)

	# Extract Horse Name
	try: horse_name = data_arr[char_horse_index][0:data_arr[char_horse_index].index('(')]
	except Exception as e: print('horse_name', e); print(data_arr)

	# Extract Jockey
	try: jockey_name = data_arr[char_horse_index][data_arr[char_horse_index].index('(')+1:data_arr[char_horse_index].index(')')]
	except Exception as e: print('horse_name', e); print(data_arr)

	# Jockey Weight
	try: jockey_weight += data_arr[char_horse_index][data_arr[char_horse_index].index(')')+1:data_arr[char_horse_index].index(')')+4]
	except Exception as e: print('jockey_weight: ', e); print(data_str)

	# SECOND HALF OF PROGRAM
	decimal_index = -1;
	for i in range(len(data_str)):
		if(data_str[i] == '.'): decimal_index = i;
		if(data_str[i] == '*'): favorite = 1;

	#Odds
	if(decimal_index == 1):
		for i in range(decimal_index-1, decimal_index+3):
			if(ord(data_str[i]) >= 48 and ord(data_str[i]) <= 57 or ord(data_str[i]) == 46): odds += data_str[i];
	elif(decimal_index >= 2):
		for i in range(decimal_index-2, decimal_index+3):
			if(ord(data_str[i]) >= 48 and ord(data_str[i]) <= 57 or ord(data_str[i]) == 46): odds += data_str[i];
	else: print('ERROR: odds')

	# Comments
	if(favorite == 1): comments += data_str[data_str.index('*')+1:]
	else: comments += data_str[data_str.index('.')+3:]

	#Variables Controlled here
	#return [horse_name, jockey_name, jockey_weight, med, equip, pole_position, odds, favorite, comments];
	return [horse_name, jockey_name, jockey_weight, pole_position, odds, favorite];


# Starts the program
main()