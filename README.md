Application to reconstruct full user sessions from raw Nevis Logs and perform a basic preprocessing to clear them.
The result is saved in an intermediate database ready to be processed for classification.

Also a tokenization step is taking place where each page requested and visited as well as usernames are replaced with
symbolic names (U1, U2... for users and P1,P2... for pages) to preserve privacy and alleviate any concerns.

Given this representation, transitions are model as a combination of referer and requested page (P1P2, P10P25, ...).

For the tokenization of pages, 5 different schemes are used leading to different results. The initial schemes uses the 
complete url to encode a page to a symbolic name (P1,P2). Schemes 1 to 4, use parts of the url to achieve a similar task.

More precisely, considering that each url contains '/' to partition the requested resource, in scheme 1 we keep only the
information before the first '/'. In scheme 2 we keep the information before the second '/' and so on. This leads to
different results, where, for example, in scheme 1 the resulting page referes only to the application (domain) that was
requested. This partitioning allows a lot of interesting experiments to be performed investigating the possibility to
correctly identify users with the minimal amount of information.

The datasets saved in the intermediate database are parsed again in Python and assembled into session strings containing
the transitions of that session as a combination of referer and requested page, (ex. P1P3 P10P11 P1P40 ... where P1 is 
the referer page, P3 is the requested page - P10 referer, P11 requested and so on).

Sequence classification algorithms (pipelines) are developed in python and the best pre-processing and classification
parameters are derived using a GridSearch approach. The results are evaluated in terms of performance (classification 
accuracy, train and test times)
