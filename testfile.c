const int a1[8] = {100, 0, -100, 10};
int a2[8] = {99999999, 99999999, 99999999};
char c1[16] = "ariana grande";

int func1(int temp){
    return temp;
}

int main() {
    if(0){

    }else if(!func1(a2[1])){

    }else if(!2){

    }else if(!0){
        int i;
        for(i = 0; i < a1[3];i = i+1){
            printf("%c",c1[i]);
        }
    }

    int a,b,c;
    a = getint();
    b = getint();
    c = getint();

    printf("%d\n",a+b+c);

    char c2[10];
    int i;
    for(i = 0; i < 10; i = i+1){
        c2[i] = getchar();
    }

    char temp = '\n';
    printf("%c",temp);

    for(i = 0; i < 10; i = i+1){
        printf("%c",c2[i]);
    }

    return 0;
}