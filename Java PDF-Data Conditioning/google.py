# import math

# def solution(area):
#     a = [];
#     val = 0;
#     while(area != 0):
#     	val = int(math.sqrt(area));
#     	a.append(val*val);
#     	area = area - (val * val);
    
#     return a;


#print(solution(15324))

# Google 2
def solution(pegs):
    a = [];
    for i in pegs:
        a.append(i);
    a = sorted(a);
    
    max_distance = a[len(a)-1] - a[0];
    
    for i in range(1, max_distance):
        radius_initial = i+1;
        last_radius = 0;

        print('Init Radius of:', i)
        for j in range(len(a)-1):
        	if(j == 0): radius_succeeding = radius_initial;
        	current_dist = (a[j+1] - a[j]);
        	print('Current Distance: ', current_dist)

        	new_radius = current_dist - radius_succeeding;
        	radius_succeeding = new_radius;
        	print('Suceeding Radius: ', new_radius)



        	if(j == len(a)-2): last_radius = radius_succeeding;
        
        if(last_radius != 0  and radius_initial/last_radius == 2): return [radius_initial, 1];
    
    return [-1, -1];
def solution(pegs):
    max_distance = pegs[len(pegs)-1] - pegs[0];
    
    for i in range(1, max_distance):
        radius_initial = i+1;
        last_radius = 0;
        for j in range(len(pegs)-1):
        	if(j == 0): radius_succeeding = radius_initial;
        	current_dist = (pegs[j+1] - pegs[j]);
        	new_radius = current_dist - radius_succeeding;
        	radius_succeeding = new_radius;
        	if(j == len(pegs)-2): last_radius = radius_succeeding;
        
        if(last_radius != 0  and radius_initial/last_radius == 2): return [radius_initial,1];
    return [-1, -1];


print(solution([4,30,50]))