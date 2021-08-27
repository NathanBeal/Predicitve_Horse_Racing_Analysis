import PyPDF2, csv, numpy as np
from Parsing_Methods import *

list_of_variables = ['LAST_DATE', 'LAST_RACE_NUM', 'LAST_TRACK', 'LAST_FINISH', 'HORSE_NAME', 'JOCKEY_NAME', 'WEIGHT', 'MED', 'EQUIP', 'POLE_POS', 'DIST_AHEAD', 'PLACE', 'ODDS', 'FAVORITE', 'COMMENTS']

def main():
	pdf = open('3-15-20 AQU.pdf', 'rb')
	pdf_reader = PyPDF2.PdfFileReader(pdf)
	print('Number of Pages: ', pdf_reader.numPages)

	init_csv(list_of_variables);
	for i in range(5,6):
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
	except Exception as e: print(35*'#');print('ERROR:', e); print(35*'#');

	return(organized_race_array)

def organize_race_array(race_array, diagnosis = False):
	for i in range(1,len(race_array)):
		# INITS
		edited_array = [];
		horse_name = jockey_name = weight = med = equip = pole_position = '';
		odds = comments = ''; favorite = 1;

		race_array = catch_line_reading_isssues(race_array);
		# HORSE'S FIRST RACE
		if(list(race_array[i][0])[0] == '-'): #first race data
			edited_array = ['-', '-', '-', '-'];
			print('ALL LINE DATA: ', race_array[i]) #MUTE HERE

			race_array[i] = organize_line_data(race_array[i], FIRST_RACE = True)

			horse_name, jockey_name, weight, med, equip, pole_position = parse_racer_data(race_array[i].pop(0));

			try: odds, favorite, comments = parse_racer_data_2(race_array[i].pop(), diagnosis);
			except Exception as e: print(); print(35*'#');print('ERROR: parse_racer_data_2', e); print(racer_data_str); print(35*'#'); print()

		else: #NOT HORSE'S FIRST RACE
			print('ALL LINE DATA: ', race_array[i]) #MUTE HERE
			for j in range(len(list_of_variables)):
				if(list_of_variables[j] == 'LAST_DATE'):     edited_array.append(race_array[i][0])
				if(list_of_variables[j] == 'LAST_RACE_NUM'): edited_array.append(race_array[i][1])
				if(list_of_variables[j] == 'LAST_TRACK'):    edited_array.append(race_array[i][2])
				if(list_of_variables[j] == 'LAST_FINISH'):   edited_array.append(race_array[i][3])

			#print('RACE: ', race_array[i]) #MUTE HERE
			try: horse_name, jockey_name, jockey_weight, med, equip, pole_position = parse_racer_data(race_array[i][4]);
			except Exception as e: print(35*'#');print('ERROR: Issue in parse_racer_data: ', e); print('STR: ', race_array[i][4]); print(race_array[i]);print(35*'#');
			try: odds, favorite, comments = parse_racer_data_2(race_array[i].pop(), diagnosis);
			except Exception as e: print(35*'#');print('ERROR: Issue in parse_racer_data_2: ', e); print('STR: ', race_array[i]); print(35*'#');

		for j in range(len(list_of_variables)):
			if(RACING_PARAMETERS('HORSE_NAME') and list_of_variables[j] == 'HORSE_NAME'):    edited_array.append(horse_name);
			if(RACING_PARAMETERS('JOCKEY_NAME') and list_of_variables[j] == 'JOCKEY_NAME'):  edited_array.append(jockey_name);
			if(RACING_PARAMETERS('WEIGHT') and list_of_variables[j] == 'WEIGHT'):            edited_array.append(weight);
			#if(list_of_variables[j] == 'MED'):  edited_array.append(med);
			#if(list_of_variables[j] == 'EQUIP'):  edited_array.append(equip);
			if(RACING_PARAMETERS('POLE_POS') and list_of_variables[j] == 'POLE_POS'):        edited_array.append(pole_position);
			#if(list_of_variables[j] == 'DIST_AHEAD'): 
			if(RACING_PARAMETERS('PLACE') and list_of_variables[j] == 'PLACE'):              edited_array.append(i);
			if(RACING_PARAMETERS('ODDS') and list_of_variables[j] == 'ODDS'):                edited_array.append(odds);
			if(RACING_PARAMETERS('FAVORITE') and list_of_variables[j] == 'FAVORITE'):        edited_array.append(favorite);
			#if(list_of_variables[j] == 'COMMENTS'):  edited_array.append(comments);
			race_array[i] = edited_array;
			if(diagnosis): print(edited_array)

	return race_array;

#
# POLE POSITION, HORSE NAME, JOCKEY NAME, WEIGHT, MEDICINE, EQUIPMENT
#
def parse_racer_data(data_str):
	# print('data_str: ', data_str) #MUTE HERE
	# Inits
	horse_name = jockey_name = weight = med = equip = pole_position = '';
	data_str = list(data_str);

	# Extract Pole Position
	try: pole_position = data_str[0]; data_str.remove(pole_position);
	except Exception as e: print('pole_position', e)

	# Extract Horse Name
	try: 
		for i in range(data_str.index('(')): horse_name += data_str[i];
	except Exception as e: print('horse_name', e); print(data_str)

	# Extract Jockey
	for i in range(data_str.index('(')+1, data_str.index(')')):
		if(data_str[i] == ','): jockey_name += NAME_SEPARATOR;
		else: jockey_name += data_str[i];


	#if()
	try: 
		for i in range (data_str.index(')')+1, data_str.index(')')+4): 
			weight += data_str[i];
	except Exception as e: print('weight: ', e); print(data_str)
	

	return horse_name, jockey_name, weight, med, equip, pole_position
#
# ODDS, FAVORITE, COMMENTS
#
def parse_racer_data_2(data_str, diagnosis = False):
	odds = comments = ''; favorite = 1;
	#print(data_str) #MUTE HERE
	data_str = list(data_str);
	for i in range(data_str.index('.')-2, data_str.index('.')+3): 
		if(ord(data_str[i]) > 45 and ord(data_str[i]) < 58): odds += data_str[i];
	try: data_str.index('*');
	except: favorite = "0"
	if(favorite == '1'): 
		for i in range(data_str.index('*')+1, len(data_str)): comments += data_str[i];
	else: 
		for i in range(data_str.index('.')+3, len(data_str)): comments += data_str[i];

	return odds, favorite, comments;



# Starts the program
main()