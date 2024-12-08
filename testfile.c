int main(){
    int x = 104;
    int flag;

    if(!(x%4) && (x%100)){
        flag = 1;
    }else{
        if(x%400 == 0){
            flag = 1;
        }
    }

    printf("%d\n",flag);

    return 0;
}