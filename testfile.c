int a,b,c,d,e,i=0;

int func0(int a,int b) {
    int i = 0;
    int c = 128;
    return (a+b)%c;
}

int func1(int a) {
    i = i + 1;
    return i;
}

int func2(int a,int b) {
    if (a % b == 0) {
        return 1;
    }
    return 0;
}

int func3() {
    printf("glo_i = %d\n",i);
    int tt0,tt1,t2,tt3,tt4,v=1906;
    for (;i < 10000;) {
        int v = a * 4 * 32 * a / a / 32;
        b = func0(b,v);
        tt0 = a*4 + b + c ;
        tt1 = a*4 + b + c + d;
        t2 = a*4 + b + c + d + e;
        tt3 = a*4 + b + c + d + e;
        tt4 = a*4 + b + c + d + e;
        if (func2(i,1000)) {
            printf("sum = %d\n", tt0 + tt1 + t2 + tt3 + tt4);
        }
        func1(i);
    }
    return tt0 + tt1 + t2 + tt3 + tt4;
}

int main() {
    int i = 0;
    a = getint();
    b = getint();
    c = getint();
    d = getint();
    e = getint();
    i = getint();
    printf("main_i = %d\n",i);
    printf("%d\n",func3());
    return 0;
}