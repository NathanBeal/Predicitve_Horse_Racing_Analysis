# Predicitve_Horse_Racing_Analysis

**Introduction**

My fascination for an algorithmic and machine learning approach to horse race betting was born after reading an article titled "The Gambler Who Cracked the Horse Racing Code" (1) in late 2018. An enthralling read, the article catalogues the life of Bill Benter and his journey in becoming one of the most prolific and profitable algorithmic sports betting icons of all time. The article left a prolific impression on me and has since driven me to investigate other statistical and algorithmic approaches to betting including Texas Hold'em and Blackjack.

**Part 1: Data Conditioning & Organization**

All data for this project was sourced from Equibase (2) and tediously downloaded. A PDF was downloaded for each day of racing for two racetracks, Aqueduct and Saratoga, spanning the course of several years. Large PDF files were constructed with a PDF combiner (3) to be conditioned for a PDF scrapping Java program. The Java program was built off the Apache PDFBox (4) library which scrapped each line of a PDF, line-by-line, to grab the large quantities of raw data available on each race. The rest of the program is centered around the organization of the data collected for each jockey and horse's placement. 

Part 2: Machine Learning & Tabular Modeling

Part 3: Weighting Data

Part 4: Making Bets 

**References**

(1) https://www.bloomberg.com/news/features/2018-05-03/the-gambler-who-cracked-the-horse-racing-code
(2) https://www.equibase.com/
(3) https://combinepdf.com/
(4) https://pdfbox.apache.org/
