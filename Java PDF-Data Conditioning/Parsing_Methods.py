import csv, random

# PDF Scrapping limits
DATE_ITERATOR_LENGTH = 10;
NAME_SEPARATOR = '/';
FILENAME = 'Data.csv'
SHUFFLE_DATA = False;

#list_of_variables = ['LAST_DATE', 'LAST_RACE_NUM', 'LAST_TRACK', 'LAST_FINISH', 'HORSE_NAME', 'JOCKEY_NAME', 'WEIGHT', 'MED', 'EQUIP', 'POLE_POS', 'DIST_AHEAD', 'PLACE', 'ODDS', 'FAVORITE', 'COMMENTS']
list_of_variables = ['LAST_DATE', 'LAST_RACE_NUM', 'LAST_TRACK', 'LAST_FINISH', 'PLACE', 'HORSE_NAME', 'JOCKEY_NAME', 'WEIGHT', 'POLE_POS', 'ODDS', 'FAVORITE']


# Retrieving Date and Race Number
def record_date_race_num(lines):
	for i in range(DATE_ITERATOR_LENGTH):
		line_pieces = lines[i].split("-")
		if(len(line_pieces) >= 2):
			date = format_date(line_pieces[1])
			race_number = format_race_num(line_pieces[2])
			return date, race_number

def format_date(raw_date_str):
	pieces = raw_date_str.split(",")
	day_month_pieces = list(pieces[0])

	month = ""; day = "";
	for i in range(len(day_month_pieces)):
		if(ord(day_month_pieces[i]) > 47 and ord(day_month_pieces[i]) < 58):
			day += day_month_pieces[i]
		else:
			month += day_month_pieces[i]

	#Assigning date and converting it
	return (str(monthToNum(month)) + '/' + day + '/' + pieces[1]);


def format_race_num(raw_race_num_str):
	pieces = list(raw_race_num_str)
	race_number = "";
	for i in range(len(pieces)):
		if(ord(pieces[i]) > 47 and ord(pieces[i]) < 58): race_number += pieces[i]

	return race_number;

#
# With direct respect to lines not being seperated correctly, line is broken off half way through and is read as seperate line
# ISSUE: AQU 3/15/20 - Race 6
#
def catch_line_reading_isssues(race_arr):
	RTN_FLAG = True;
	for i in range(len(race_arr)):
		if(len(list(race_arr[i][0])) < 3): RTN_FLAG = False;
	if(RTN_FLAG): return race_arr;

	new_race_arr = []; temp_line = race_arr.pop(0);
	bad_split_count_index  = [];

	race_arr.reverse()

	while len(race_arr) > 0:
		if(len(list(race_arr[0][0])) >= 3):
			#print('GOOD: ', race_arr[0])
			new_race_arr.append(race_arr.pop(0))
		else:
			#print('BAD: ', race_arr[0])
			for j in range(len(race_arr[0])):
	 			race_arr[1].append(race_arr[0][j])

			race_arr.pop(0)
			new_race_arr.append(race_arr.pop(0))

	new_race_arr.reverse(); new_race_arr.insert(0, temp_line);
	# for i in range(len(new_race_arr)): print(new_race_arr[i]); print()
	return new_race_arr;
	

#
# HANDLES ALL ISSUES THAT OCCUR WHEN DATA IS READ FROM PDF
#
def organize_line_data(line_data_array, FIRST_RACE = False):
	if(FIRST_RACE):
		line_data_array[0] = line_data_array[0].split('---')[1] #Pulls --- off of it

		if(len(line_data_array) == 1):
			split_arr = line_data_array[0].split(')');
			split_arr[0] = split_arr[0] + ')' + split_arr[1][0:5]
			split_arr[1] = split_arr[1][6:]
			split_arr_2 = split_arr[1].split('.');
			split_arr_2[0] += " "
			split_arr_2[1] = split_arr_2[0][-3:-1] + '.' + split_arr_2[1] 
			split_arr_2[0] = split_arr_2[0].split(split_arr_2[0][-3:-1])[0] # remove last two digits which are hopefully the odds
			split_arr.append(split_arr_2[0]); split_arr.append(split_arr_2[1]) #Recombine lists
			line_data_array = split_arr

		try: line_data_array.remove('»')
		except Exception as e: count = 1;
		try: line_data_array.remove('½')
		except Exception as e: count = 1;

		# Find where the ')' Character is
		char_index = -1;
		for i in range(len(line_data_array)):
			try:
				line_data_array[i].index(')');
				char_index = i;
			except Exception as e: count = 0;

		# Detaching pole pos from name
		if(ord(line_data_array[char_index][0]) >= 48 and ord(line_data_array[char_index][0]) <= 57):
			line_data_array.insert(char_index,line_data_array[char_index][0])
			line_data_array[char_index+1] = line_data_array[char_index+1][1:]
			char_index += 1;

		# Tacks weight and medicine onto back of string if not present
		if(list(line_data_array[0])[-1] == ')'): 
			line_data_array[0] += line_data_array.pop(1); 
			line_data_array[0] += line_data_array.pop(1);

		return line_data_array, char_index;
	else:
		try: line_data_array.remove('»')
		except Exception as e: count = 1;
		try: line_data_array.remove('½')
		except Exception as e: count = 1;

		# Find where the ')' Character is
		char_index = -1;
		for i in range(len(line_data_array)):
			try:
				line_data_array[i].index(')');
				char_index = i;
			except Exception as e: count = 0;

		# Detaching pole pos from name
		if(ord(line_data_array[char_index][0]) >= 48 and ord(line_data_array[char_index][0]) <= 57):
			line_data_array.insert(char_index,line_data_array[char_index][0])
			line_data_array[char_index+1] = line_data_array[char_index+1][1:]
			char_index += 1;

		if(list(line_data_array[char_index])[-1] == ')'):
			line_data_array[char_index] += line_data_array.pop(char_index+1); 
			line_data_array[char_index] += line_data_array.pop(char_index+1);
			
		return line_data_array, char_index




def monthToNum(month): return {'January': 1, 'Febuary': 2, 'March': 3,'April': 4, 'May': 5,'June': 6, 'July': 7,'August': 8,'September': 9,'October': 10,'November': 11,'December': 12}[month]
def RACING_PARAMETERS(var): return {'LAST_DATE': True, 'LAST_RACE_NUM': True, 'LAST_TRACK': True, 'LAST_FINISH': True, 'HORSE_NAME': True, 'JOCKEY_NAME': True, 'WEIGHT': True, 
									'MED': False, 'EQUIP': False, 'POLE_POS': True, 'DIST_AHEAD': False, 'PLACE': True, 'ODDS': True, 'FAVORITE': True, 'COMMENTS': False}[var]

def write_to_csv(racing_data, first_race = False):
	with open(FILENAME, 'a', newline='') as FILE: 
		racing_data.pop(0);
		if(SHUFFLE_DATA): random.shuffle(racing_data);
		writer = csv.writer(FILE) 
		writer.writerows(racing_data)

def init_csv(variables_list):
	header_list = [];
	for i in range(len(variables_list)):
		if(RACING_PARAMETERS(variables_list[i])): header_list.append(variables_list[i]);
	with open(FILENAME, 'w', newline='') as FILE: 
			writer = csv.writer(FILE)
			writer.writerow(header_list); 
