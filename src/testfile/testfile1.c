// 1. const
const int a = 1;
const int b = 1, c = 2;
const int d[5] = {0,1,2,3,4};
const int e[5] = {0,1,2,3,4}, f[5] = {0,1,2,3,4};

const char g = 'g';
const char h = 'h', i = 'i';
const char j[5] = {'0','1','2','3','4'};
const char k[5] = {'0','1','2','3','4'}, l[5] = {'0','1','2','3','4'};
const char a10[5] = "1234";

// 2. var
int m;
int n, o;
int p[1];
int q[1], r[1];

int s = 1;
int t = 1, u = 1;
int v[1] = {1};
int w[1] = {1}, x[1] = {1};

char y;
char z, aa;
char bb[1];
char cc[1], dd[1];

char ee = '1';
char ff = '1', gg = '1';
char hh[1] = {'1'};
char ii[1] = {'1'}, jj[1] = {'1'};
char a11[5] = "1234";

// 3. func
void kk(){
    return ;
}
void ll(int a){}
void mm(int a, int b){}
void nn(int a[]){}
void oo(int a[], int b[]){}

int pp(){
    return 0 + 1;
}
int qq(int a){
    if (a == 1) {
        return 2;
    } else {
        return 1;
    }
}
int rr(int a, int b){
    return pp();
}
int ss(int a[]){
    return 1;
}
int tt(int a[], int b[]){
    return 1;
}      

char uu(){
    return 'c';
}
char vv(char a){
    return 'c';
}
char ww(char a, char b){
    return 'c';
}
char xx(char a[]){
    return 'c';
}
char yy(char a[], char b[]){
    return 'c';
}

// 4. main
int main(){
    // 1. 赋值
    const int a1 = 1;
    const int a2 = 1, a3 = 1;
    const int a12[5] = {0,1,2,3,4};
    const char a4 = 'c';
    const char a5 = 'c', a6 = 'c';
    const char a13[5] = "1234";
    int xx;
    int zz = 1;
    int a_a = 1, b_b = 1;
    int a14[5] = {0,1,2,3,4};
    char c_c;
    char d_d = 'c';
    char e_e = 'c', f_f = 'c';
    char a15[5] = "1234";
    int a7 = 1 + 1;
    int a8 = a_a + b_b;
    int a9 = pp();
    a9 = a9*a9;
    a14[0] = 0;
    // 2. 表达式
    ;
    1;
    'c';
    (2);
    pp();
    qq(1);
    +pp();
    +qq(1);
    +rr(1,2);
    qq(1) - qq(1);
    rr(1,2);
    rr(a,1);
    rr(a,b);
    +1;
    -1;
    !1;
    1+1;
    1-1;
    1*1;
    1/1;
    1%1;

    (1+1)*(1+1)/(1-2);

    1+'c';
    1+(2);
    1+rr(1,2);
    1-'c';
    1-(2);
    1-rr(1,2);
    1*'c';
    1*(2);
    1*rr(1,2);
    1/'c';
    1/(2);
    1/rr(1,2);
    1%'c';
    1%(2);
    1%rr(1,2);

    zz+1;
    zz-1;
    zz*1;
    zz/1;
    zz%1;
    (zz+rr(1,2))/zz;
    // 3. {}
    {}
    {
        int g_g = 1;
        {
            int g_g = 2;
        }
    }
    // 4. if(); if(){} if(){}else{}
    if(!1);
    if(zz != 1);
    if(zz == 1){
        zz = zz + 1;
    }
    if (zz == 1) {
        zz = zz + 1;
    } else {
        zz = 1;
    }
    if (a != 1 && a == 1);
    if (a == 2 || a == 1);
    // 5. for(;;){}
    int i_vice;
    for(;;){
        break;
    }

    i_vice = 1;
    for(; i_vice < 2; i_vice = i_vice + 1){}
    for(i_vice = 1; ; i_vice = i_vice + 1){
        break;
    }
    for(i_vice = 1; i_vice < 2;){
        break;
    }

    i_vice = 1;
    for(; ; i_vice = i_vice + 1){
        break;
    }
    i_vice = 1;
    for(;i_vice < 2;){
        break;
    }
    for(i_vice = 1;;){
        break;
    }

    for(i_vice = 1; i_vice < 2; i_vice = i_vice + 1){}
    for(i_vice = 1; i_vice <= 2; i_vice = i_vice + 1){}
    for(i_vice = 1; i_vice > 2; i_vice = i_vice + 1){}
    for(i_vice = 1; i_vice >= 2; i_vice = i_vice + 1){}
    for(i_vice = 1; i_vice > 0; i_vice = i_vice - 1){}
    for(i_vice = 1; i_vice >= 0; i_vice = i_vice - 1){}
    // 6. break;
    for(i_vice = 1; i_vice >= 0; i_vice = i_vice - 1){
        break;
    }
    // 7. continue;
    // printf("for-09\n");
    for(i_vice = 1; i_vice >= 0; i_vice = i_vice - 1){
        continue;
    }
    // 8. return 1;
    // 9. 赋值 + getint();
    int h_h;
    h_h = getint();
    // 10. 赋值 + getchar();
    char i_i;
    i_i = getchar();

    // 11. 十行输出
    printf("21375077\n");
    int i;
    for(i = 0; i < 9; i = i + 1) {
        if (i == 0) {
            printf("%d %d\n", pp(), rr(pp(),1));
        } else {
            printf("%d\n", i + 1);
        }
    }

    return 0;
}