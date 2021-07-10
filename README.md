# Predicitve_Horse_Racing_Analysis

**Introduction**

My fascination for an algorithmic and machine learning approach to horse race betting was born after reading an article titled "The Gambler Who Cracked the Horse Racing Code" (1) in late 2018. An enthralling read, the article catalogues the life of Bill Benter and his journey in becoming one of the most prolific and profitable algorithmic sports betting icons of all time. The article left a prolific impression on me and has since driven me to investigate other statistical and algorithmic approaches to betting including Texas Hold'em and Blackjack.

**Part 1: Data Conditioning & Organization**

All data for this project was sourced from Equibase (2) and tediously downloaded. A PDF was downloaded for each day of racing for two racetracks, Aqueduct and Saratoga, spanning the course of several years. Large PDF files were constructed with a PDF combiner (3) to be conditioned for a PDF scrapping Java program. The Java program was built off the Apache PDFBox (4) library which scrapped each line of a PDF, line-by-line, to grab the large quantities of raw data available on each race. The rest of the program is centered around the organization of the data collected for each jockey and horse's placement. Parameters such as horse name, jockey name, distance ran, racing odds and several other variables are written out to a CSV. A one is recorded in the 'finalrank' column if the horse placed in first, second or third in a race and a two is recorded in the column if it comes in any other position.

**Part 2: Machine Learning & Tabular Modeling**

The CSV is then loaded into the Jupyter Notebook and different parameters are pulled into the machine learning model depending upon the discrete and contiuous data that is desired. 80% of the data is set aside for the training set and the other 20% is used for the validation set. The tabular model is trained and fit with an ideal learning rate after the choosing an ideal learning rate from the plotted graph. A third set of data is passed into the model, a set which contains the data for a specific race, and the predictions are made. The names of the horses are assigned to the prediction values and ordered from most to least likely to finish within the top three spots. The projected chances are then converted into new, model-produced odds and the predictions are compared against the actual outcome of the race to evaluate how the model fared. 

**Part 3: Weighting Data**

**Part 4: Making Bets**

**Further Improvements**
 - Convert the Java PDF processing program to a Python based one

**References**

(1) https://www.bloomberg.com/news/features/2018-05-03/the-gambler-who-cracked-the-horse-racing-code

(2) https://www.equibase.com/

(3) https://combinepdf.com/

(4) https://pdfbox.apache.org/

(X) https://projects.ncsu.edu/crsc//reports/ftp/pdf/crsc-tr06-19.pdf
