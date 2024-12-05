const int constIntArray[3] = {10, 20, 30};
const char constCharArray[5] = {'A', 'B', 'C', 'D', 'E'};
int intArray[5];
char charArray[5];

int func_with_param(int a, char b, int arr[], char str[]) {
    // 1. 取出arr[0], 也就是intArray[0]有问题
    // 2. intArray[0]本身在内存中的数值没问题
    printf("Function with parameters: a = %d, b = %c arr[0] = %d, str[0] = %c\n", a, b, arr[0], str[0]);
    int sum = a + b + arr[0] + str[0];
    printf("Sum in func_with_param: %d\n", sum);
    return sum;
}

int main() {
    // calculate
    // intArray[5]:10, 20, 30, 30, 60
    // intArray[5]:10, 1, 0, 30, 60
    // charArray[5]:79, 0, 0, 0, 0
    intArray[0] = constIntArray[0];
    intArray[1] = constIntArray[1];
    intArray[2] = constIntArray[2];
    intArray[3] = intArray[0] + intArray[1];
    intArray[4] = intArray[3] + intArray[2];

    intArray[0] = -intArray[0];

    intArray[0] = +intArray[0];

    intArray[1] = intArray[3] / intArray[2];
    intArray[2] = intArray[3] % intArray[2];

    charArray[0] = constCharArray[0] + constCharArray[1] + constCharArray[2] + constCharArray[3] + constCharArray[4];

    int result = func_with_param(intArray[0], charArray[0], intArray, charArray);

    return 0;
}