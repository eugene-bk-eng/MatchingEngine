### Matching Engine Project

Editor:
https://pandao.github.io/editor.md/en.html

- Standard matching on price and time priority


![](https://pandao.github.io/editor.md/images/logos/editormd-logo-180x180.png)

![](https://img.shields.io/github/stars/pandao/editor.md.svg) ![]


**Table of Contents**

LOGGING

In runtime, engine uses log4j-core
In test configuration, because we use logging Captor library, 
I use ch.qos.logback. The library is a little buggy but does the job.
It's hard to configure its dependency.


###Images

Image:

![](https://pandao.github.io/editor.md/examples/images/4.jpg)

--------------------------------------
---EXCH BID: buys from client | EXCH OFFER: sells to client
                                100 x 10.15
                                300 x 10.12
                                100 x 10.09
100 x 10.05
300 x 10.02
200 x 9.80

  you sell at BID            | you buy at OFFER
  
Highest exchange BID is on top = best price exch willing
to pay for your stock

Lowest exchange OFFER is on top = best(lowest) price 
exch is willing to sell its stock for.

In other words book prices are sorted with respect to what
is best for the client who either 
BUYS = spends money and get exch stock
SELLS = gets money for his stock.