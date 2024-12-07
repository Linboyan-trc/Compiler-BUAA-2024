int global_var1 = 10;
char global_char = 'A';
int global_arr[5] = {1, 2, 3, 4, 5};
char global_str[5] = {'H', 'e', 'l', 'l', 'o'};

int compute_sum(int a, int b, int c, int d) {
    if (a > b || c < d) {
        return a + b + c + d;
    } else {
        return a - b + c - d;
    }
}

char find_max_char(char arr[], int size) {
    char max = arr[0];
    int i;
    for (i = 1; i < size; i = i + 1) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    return max;
}

int process_array(int arr[], int size, int threshold) {
    int sum = 0;
    int i;
    for (i = 0; i < size; i = i + 1) {
        if (arr[i] > threshold && arr[i] % 2 == 0) {
            sum = sum + arr[i];
        } else {
            sum = sum - arr[i];
        }
    }
    return sum;
}

void print_global_data() {
    printf("%d\n", global_var1);
    printf("%c\n", global_char);
    printf("%c\n", find_max_char(global_str, 5));
}

int calculate_complex(int a, int b, int c, int d, int threshold, char sample[]) {
    int sum = compute_sum(a, b, c, d);
    int arr_result = process_array(global_arr, 5, threshold);
    char max_char = find_max_char(sample, 5);
    if (arr_result > sum || max_char == 'Z') {
        return arr_result + sum;
    }
    if (arr_result < sum && max_char != 'Z') {
        return sum - arr_result;
    } else {
        return 0;
    }
}

int main() {
    int a = 5;
    int b = 10;
    int c = 15;
    int d = 20;
    char sample[5] = {'a', 'b', 'c', 'd', 'e'};
    int result = calculate_complex(a, b, c, d, 8, sample);
    print_global_data();
    printf("%d\n", result);
    return 0;
}
