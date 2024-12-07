const int MAX_SIZE = 30;
const int MULTIPLIER = 7;
const int OFFSET = 5;
const int THRESHOLD = 15;

// 全局变量
int global_values[MAX_SIZE];
int global_results[MAX_SIZE];
const char valid_chars[6] = {'A', 'B', 'C', 'D', 'E', 'F'};

//int getchar(){
// char c;
// scanf("%c",&c);
// return (int)c;
//}
//
//int getint(){
// int t;
// scanf("%d",&t);
// while(getchar()!='\n');
// return t;
//}

// 函数：初始化全局变量
void initialize_global_values() {
    int i;
    for (i = 0; i < MAX_SIZE; i = i + 1) {
        global_values[i] = (i * MULTIPLIER + OFFSET) % 100;
        global_results[i] = 0;
    }
}

// 函数：对数组进行模运算
void mod_array(int arr[], int size, int mod) {
    int i;
    for (i = 0; i < size; i = i + 1) {
        arr[i] = arr[i] % mod;
    }
}

// 函数：对数组进行加权求和
int weighted_sum(int arr[], int size, int weight) {
    int sum = 0;
    int i;
    for (i = 0; i < size; i = i + 1) {
        sum = sum + arr[i] * weight;
    }
    return sum;
}

// 函数：条件过滤数组
int filter_array(int arr[], int size, int threshold) {
    int sum = 0;
    int i;
    for (i = 0; i < size; i = i + 1) {
        if (arr[i] > threshold || arr[i] < -threshold) {
            sum = sum + arr[i];
        }
    }
    return sum;
}

// 函数：生成有效字符的统计
int count_valid_chars(char arr[], int size) {
    int count = 0;
    int i;
    for (i = 0; i < size; i = i + 1) {
        int j;
        for (j = 0; j < 6; j = j + 1) {
            if (arr[i] == valid_chars[j]) {
                count = count + 1;
                break;
            }
        }
    }
    return count;
}

// 函数：混合逻辑计算
int complex_computation(int arr[], int size, int mod, int threshold) {
    int weighted = weighted_sum(arr, size, mod);
    if (weighted > 500) {
        return filter_array(arr, size, threshold);
    } else if (weighted < -500) {
        return weighted % threshold;
    } else {
        return weighted;
    }
}

int main() {
    int primary[MAX_SIZE];
    int auxiliary[MAX_SIZE];
    char char_data[MAX_SIZE];
    int i;

    // 全局变量初始化
    initialize_global_values();

    // 动态填充 primary 和 auxiliary 数组
    printf("Enter %d integers for primary array:\n", MAX_SIZE);
    for (i = 0; i < MAX_SIZE; i = i + 1) {
        int temp;
        temp = getint();
        primary[i] = temp % 100;
    }

    printf("Enter %d integers for auxiliary array:\n", MAX_SIZE);
    for (i = 0; ; i = i + 1) {
        if (i == MAX_SIZE) break;
        int temp;
        temp = getint();
        auxiliary[i] = temp % 50 - OFFSET;
    }

    // 填充字符数组
    printf("Enter %d integers for char array:\n", MAX_SIZE);
    for (i = 0; ; ) {
        if (i == MAX_SIZE) {
            break;
        }
        int temp;
        temp = getint();
        char_data[i] = valid_chars[temp % 6];
        i = i + 1;
    }

    // 使用新的作用域执行操作
    {
        int filtered_sum;
        filtered_sum = filter_array(primary, MAX_SIZE, THRESHOLD);
        printf("Filtered sum of primary: %d\n", filtered_sum);
    }

    {
        int weighted_result;
        mod_array(auxiliary, MAX_SIZE, 20);
        weighted_result = weighted_sum(auxiliary, MAX_SIZE, 2);
        printf("Weighted sum of auxiliary: %d\n", weighted_result);
    }

    // 动态条件分支
    for (; ; ) {
        if (global_values[0] > 0) {
            global_results[0] = complex_computation(primary, MAX_SIZE, OFFSET, THRESHOLD);
        } else if (global_values[0] == 0) {
            global_results[0] = weighted_sum(auxiliary, MAX_SIZE, 3);
        } else {
            break;
        }
        break;
    }

    // 打印全局结果
    printf("Global result: %d\n", global_results[0]);

    // 多层 if-else 检查字符数据
    {
        int valid_count;
        valid_count = count_valid_chars(char_data, MAX_SIZE);
        if (valid_count > 10) {
            printf("Valid chars exceed 10\n");
        } else if (valid_count > 5) {
            printf("Valid chars exceed 5\n");
        } else {
            printf("Few valid chars\n");
        }
    }

    return 0;
}