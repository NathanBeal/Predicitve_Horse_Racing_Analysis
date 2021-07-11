# PDF Scrapping limits
DATE_ITERATOR_LENGTH = 10;
NAME_SEPARATOR = '/';


# Retrieving Date and Race Number
def record_date_race_num(lines):
	for i in range(DATE_ITERATOR_LENGTH):
		line_pieces = lines[i].split("-")
		if(len(line_pieces) >= 2):
			date = format_date(line_pieces[1])
			race_number = format_race_num(line_pieces[2])
			#print(date); print(race_number)
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
		if(ord(pieces[i]) > 47 and ord(pieces[i]) < 58):
			race_number += pieces[i]
	return race_number;

def monthToNum(month): return {'January': 1, 'Febuary': 2, 'March': 3,'April': 4, 'May': 5,'June': 6, 'July': 7,'August': 8,'September': 9,'October': 10,'November': 11,'December': 12}[month]
def RACING_PARAMETERS(var): return {'LAST_DATE': True, 'LAST_RACE_NUM': True, 'LAST_TRACK': True, 'LAST_FINISH': True, 'HORSE_NAME': True, 'JOCKEY_NAME': True, 'WEIGHT': True, 
									'MED': False, 'EQUIP': False, 'POLE_POS': True, 'DIST_AHEAD': False, 'PLACE': True, 'ODDS': True, 'FAVORITE': True, 'COMMENTS': False}[var]



