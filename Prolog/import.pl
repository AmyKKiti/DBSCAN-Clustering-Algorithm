import:-
    csv_read_file('partition65.csv', Data65, [functor(partition)]),maplist(assert, Data65),
    csv_read_file('partition74.csv', Data74, [functor(partition)]),maplist(assert, Data74),
    csv_read_file('partition75.csv', Data75, [functor(partition)]),maplist(assert, Data75),
    csv_read_file('partition76.csv', Data76, [functor(partition)]),maplist(assert, Data76),
    csv_read_file('partition84.csv', Data84, [functor(partition)]),maplist(assert, Data84),
    csv_read_file('partition85.csv', Data85, [functor(partition)]),maplist(assert, Data85),
    csv_read_file('partition86.csv', Data86, [functor(partition)]),maplist(assert, Data86),listing(partition).



%%%% PREDICATE mergeClusters/1 %%%%

% mergeClusters/1
% Performs the merge step on our database, returns the merged result in L.
% mergeClusters(L)

mergeClusters(L) :- findall([D,X,Y,C], partition(_,D,X,Y,C),DB),
                    processClusters(DB, [], L).

% test predicate mergeClusters/1
testMerge(mergeClusters) :-
    write('mergeClusters(Result)'),
    nl,
    mergeClusters(Result),
    write(Result).

%%%% END OF mergeClusters/1 %%%%



%%%% PREDICATE processClusters/3 %%%%
% Processes each element T of database : 
% checks if T is a member of cluster list already 
% if T is not a member = append to clusterlist
% if T is a member = change labels
% processClusters(DATABASE, CLUSTERLIST, OUTCLUSTERLIST)

% base case
processClusters([], OUT, OUT) :- !.

% if it is not a member : add to clusterlist
processClusters([T|Q], CLSLST, OUT) :- 
    notMemberOfClusterList(T,CLSLST), !, 
    insertInClusterList(CLSLST, T, NEWCLST),
    processClusters(Q, NEWCLST, OUT). % recursive call, keep processing

% else it IS a member (intersecting),
% we need to :
% 1. find set I (the point from clusterlist which is the same as T)
% 2. change labels of all points from clusterlist with the same label as T

processClusters([T|Q], CLSLST, OUT) :-
    findSame(CLSLST, T, FOUND), % ind the point which is the same
    getLabel(FOUND, LABELOLD), % find the old label
    getLabel(T, LABELNEW), % find the new label
    relabel(LABELOLD,LABELNEW,CLSLST, NEWLST), % change labels
    processClusters(Q, NEWLST, OUT). % recursive call, keep going!

% test predicate processClusters/3
% expected output : Result = [[15,10,15,1],[14,10,14,1],[13,10,13,1],[16,10,21,1],[17,10,22,1],[19,10,23,1],[11,10,11,1],[12,10,12,1]]
 
testProcess(processClusters) :-
    write('processClusters([[11, 10, 11, 1],
                            [12, 10, 12, 1],
                            [13, 10, 13, 1],
                            [14, 10, 14, 1],
                            [15, 10, 15, 1]],
                            
                            [[15, 10, 15, 2],
                            [14, 10, 14, 2],
                            [13, 10, 13, 2],
                            [16, 10, 21, 2],
                            [17, 10, 22, 2],
                            [19, 10, 23, 2]], Result)'),
    nl,

    processClusters(        [[11, 10, 11, 1] ,
                            [12, 10, 12, 1],
                            [13, 10, 13, 1],
                            [14, 10, 14, 1],
                            [15, 10, 15, 1]],
                            
                           [[15, 10, 15, 2],
                            [14, 10, 14, 2],
                            [13, 10, 13, 2],
                            [16, 10, 21, 2],
                            [17, 10, 22, 2],
                            [19, 10, 23, 2]], Result),

    write(Result).
    									

%%%% END OF processClusters %%%%	



%%%% PREDICATE samePt/2 %%%%

% samePt/2
% samePt will check if 2 points have the same PointID
% samePt(POINTA, POINTB)

samePt([B,_,_,_], [B,_,_,_]) :- !.

% test predicate samePt/2
% expected output : true
testSamePt(samePt) :-
    write('samePt([115,0,3,1],[115,0,3,2])'),
    nl,
    samePt([115,0,3,1],[115,0,3,2]).

%%%% END of samePt/2 %%%%



%%%% PREDICATE insertInClusterList/3 %%%%

% insertInClusterList/3
% Inserts the IN point at the end in the clusterlist. 
% returns the output list in OUT
% insertInClusterList(Clusterlist, IN, OUT)

insertInClusterList(CLSLIST, IN, OUT) :- append(CLSLIST, [IN], OUT).

% test predicate insertInClusterList/3
% expected output : [[115,0,3,1], [116,0,5,2], [69,0,5,4]]
testInsert(insertInClusterList) :-
    write('insertInClusterList([[115,0,3,1],[116,0,5,2]], [69,0,5,4], Result)'),
    nl,
    insertInClusterList([[115,0,3,1],[116,0,5,2]], [69,0,5,4], Result),
    write(Result).

%%%% END OF insertInClusterList %%%%



%%%% PREDICATE notMemberOfClusterList/2 %%%%

% notMemberOfClusterList/2
% returns True if an input point IN is NOT already in the clusterlist (depending on their pointID)
% returns False if the point is a member of clusterlist
% notMemberOfClusterList(IN, Clusterlist)

notMemberOfClusterList(_,[]) :- !. %base case, if list empty, it is not member.
notMemberOfClusterList(IN, [T|Q]) :- not(samePt(IN, T)),
    							notMemberOfClusterList(IN, Q).

% test predicate
% expected output : True
testMember(notMemberOfClusterList) :-
    write('notMemberOfClusterList([69,0,5,4], [[115,0,3,1],[116,0,5,2]])'),
    nl,
    notMemberOfClusterList([69,0,5,4], [[115,0,3,1],[116,0,5,2]]).

%%%% END OF notMemberOfClusterList/3 %%%%



%%%% PREDICATE findSame/3 %%%%

% findSame/3
% returns the point in clusterlist which has the same PointID as input point IN.
% findSame(CLSLST, IN , FOUND)

findSame([T|Q], IN, FOUND) :- not(samePt(T,IN)), !, findSame(Q,IN,FOUND).
findSame([T|_], _, T) :- !.

% test predicate findSame/3
% expected output : [115,0,3,1]
testFind(findSame) :-
    write('findSame([[115,0,3,1],[116,0,5,2]], [115,0,3,5], Result)'),
    nl,
    findSame([[115,0,3,1],[116,0,5,2]], [115,0,3,5], Result),
    write(Result).

%%%% END OF findSame/3 %%%%



%%%% PREDICATE getLabel/2 %%%%

% getLabel/2
% returns the label of a point
% getLabel(POINT, LABEL)

getLabel([_,_,_,LABEL], LABEL).

% test predicate getLabel/2
% expected output : 4

testGetLabel(getLabel) :-
    write('getLabel([1,2,3,4], Result)'),
    nl,
    getLabel([1,2,3,4], Result),
    write(Result).

%%%% END OF getLabel %%%%



%%%%%%% PREDICATE relabel/4 %%%%%%%%%

% relabel/4 
% relabels the points of cluster BEFORE with label AFTER 
% relabel(BEFORE, AFTER, IN, OUT)

% BEFORE = label to replace 
% AFTER = the new label after replacing
% IN = input list
% OUT = result output list	

relabel(_,_, [],[]) :- !.
relabel(BEFORE, AFTER, [T|Q], [X|Y]):- change(BEFORE, AFTER, T, X),
    								relabel(BEFORE, AFTER, Q, Y).

%test predicate relabel/4
% expected output : [[1,2.2,3.1,77], [2,2.1,3.1,22], [3,2.5,3.1,77], [4,2.1,4.1,77], [5,4.1,3.1,30]]
testRelabel(relabel) :- 
write('relabel(33, 77, [[1,2.2,3.1,33], [2,2.1,3.1,22], [3,2.5,3.1,33], [4,2.1,4.1,33],[5,4.1,3.1,30]],Result)'),
nl, 
relabel(33, 77, [[1,2.2,3.1,33], [2,2.1,3.1,22], [3,2.5,3.1,33], [4,2.1,4.1,33], [5,4.1,3.1,30]],Result), 
write(Result).


% changeOne/3
% changes label of list IN with VALUE and outputs in OUT
% changeOne(VALUE, IN, OUT)

changeOne(VALUE, [A,B,C,_], [A,B,C, VALUE]).

%test predicate changeOne/3
% expected output : [1, 2.2, 3.1, 77]
testChangeOne(changeOne) :-
    write('changeOne(77, [1, 2.2, 3.1, 33], Result)'),
    nl,
    changeOne(77, [1, 2.2, 3.1, 33], Result),
    write(Result).


% change/4
% changes the label BEFORE to AFTER only if the label = BEFORE
% change(BEFORE, AFTER, LISTIN, LISTOUT)

change(BEFORE, AFTER, [A,B,C,BEFORE], LISTOUT) :-
    			changeOne(AFTER, [A,B,C,BEFORE], LISTOUT), !.
change(_, _, SAME, SAME).

% test predicate change/4
% change(33,77,[1,2.2,3.1,22],L). returns false
% expected output : [1, 2.2, 3.1, 77]
testChange(change) :-
    write('change(22, 77, [1, 2.2, 3.1, 22], Result)'),
    nl,
    change(22,77,[1, 2.2, 3.1, 22], Result),
    write(Result).

%%%%%%% END OF RELABEL/4 %%%%%%%%%

