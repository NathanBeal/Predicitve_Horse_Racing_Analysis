import urllib.request
import os
'''
pdf_path = "C:/Users/natha/Desktop/GitHub Repositories/Active Repos/Predicitve_Horse_Racing_Analysis"

def download_file(download_url, filename):
	url = 'https://www.equibase.com/static/chart/pdf/SAR090121USA.pdf';
	response = requests.get(url)



    response = urllib.request.urlopen(download_url)    
    file = open(filename + ".pdf", 'wb')
    file.write(response.read())
    file.close()
 
download_file(pdf_path, "Test")

#https://www.equibase.com/premium/eqbPDFChartPlus.cfm?RACE=A&BorP=P&TID=SAR&CTRY=USA&DT=08/14/2021&DAY=D&STYLE=EQB
''''''
https://www.equibase.com/premium/eqbPDFChartPlus.cfm?RACE=A&BorP=P&TID=SAR&CTRY=USA&DT=07/15/2021&DAY=D&STYLE=EQB
https://www.equibase.com/static/chart/pdf/SAR090121USA.pdf
'''
'''
# Import libraries
import requests
from bs4 import BeautifulSoup
  
# URL from which pdfs to be downloaded
url = "https://www.geeksforgeeks.org/how-to-extract-pdf-tables-in-python/"
  

# Requests URL and get response object
response = requests.get(url)
  
# Parse text obtained
soup = BeautifulSoup(response.text, 'html.parser')
  
# Find all hyperlinks present on webpage
links = soup.find_all('a')
  
i = 0
  
# From all links check for pdf link and
# if present download file
for link in links:
    if ('.pdf' in link.get('href', [])):
        i += 1
        print("Downloading file: ", i)
  
        # Get response object for link
        response = requests.get(link.get('href'))
  
        # Write content in pdf file
        pdf = open("pdf"+str(i)+".pdf", 'wb')
        pdf.write(response.content)
        pdf.close()
        print("File ", i, " downloaded")
  
print("All PDF files downloaded")
'''
'''
# imported the requests library
import requests
image_url = "https://www.python.org/static/community_logos/python-logo-master-v3-TM.png"
  
# URL of the image to be downloaded is defined as image_url
r = requests.get(image_url) # create HTTP response object
  
# send a HTTP request to the server and save
# the HTTP response in a response object called r
with open("python_logo.png",'wb') as f:
  
    # Saving received content as a png file in
    # binary format
  
    # write the contents of the response (r.content)
    # to a new file in binary mode.
    f.write(r.content)
    '''

import requests
urls = ['https://www.equibase.com/static/chart/pdf/SAR090121USA.pdf']
output_dir = 'C:/Users/natha/Desktop/GitHub Repositories/Active Repos/Predicitve_Horse_Racing_Analysis'

for url in urls:
	response = requests.get(url);
	if(response.status_code == 200):
		file_path = os.path.join(output_dir, os.path.basename(url))
		with open(file_path, 'wb') as f:
			f.write(response.content)