from datetime import date, datetime, timedelta
import pandas as pd, yfinance as yf, numpy as np
import matplotlib.pyplot as plt



amt_reinvested = 10;
timeline = 3

years_invested = 5;
percent_allocation = -1;
tickers = ['MSFT', 'AAPL']

data = []
for i in range(len(tickers)):
	# end = datetime(datetime.now().year, datetime.now().month, datetime.now().day)#).strftime('%m/%d/%Y %H:%M:%S')
	# start = datetime.timedelta(365*years_invested)
	# start = start - end;
	# print(start)
	price_history = yf.Ticker(tickers[i]).history(start="2015-01-01", end="2020-12-20", interval = '1wk')
	price_history = price_history['Open']
	clean_data = [];
	for i in range(len(price_history)): clean_data.append(round(price_history[i],2))
	print(clean_data)

	print(data)


# plot1 = plt.figure(1)
# x = list(range(0, days))
# plt.plot(x, prices)
# plt.title(ticker)
# plt.xlabel("Days")
# plt.ylabel("Price")

# plt.plot(x, f1.averages)
# plt.plot(x, f2.averages)
# plt.plot(x, f3.averages)
# plt.plot(x, f4.averages)

# plt.show()