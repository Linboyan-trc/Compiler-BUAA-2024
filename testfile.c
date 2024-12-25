// 正确的常量和变量声明
const int const1 = 42; // 定义常量
int global_var = 0;    // 定义全局变量

// 错误 1：非法符号 a
void test_illegal_symbol() {
    if (global_var | const1) { // 非法符号（错误类型 a）
        global_var = 1;
    }
}

// 错误 2：名字重定义 b
void test_name_redefinition() {
    int x = 10;   // 正确
    int x = 20;   // 名字重定义（错误类型 b）
}

// 错误 3：未定义的名字 c
void test_undefined_name() {
    int result = undefined_var + 1; // 未定义名字（错误类型 c）
}

// 错误 4：函数参数个数不匹配 d
void test_func_param_count(int x, int y) {}
void test_param_count_error() {
    test_func_param_count(1); // 参数个数不匹配（错误类型 d）
}

// 错误 5：函数参数类型不匹配 e
void test_func_param_type(int x, int arr[]) {}
void test_param_type_error() {
    char invalid_param[5];
    test_func_param_type(1, invalid_param); // 参数类型不匹配（错误类型 e）
}

// 错误 6：void 函数有返回值 f
void test_void_return_value() {
    return 1; // 错误：void 函数不能有返回值（错误类型 f）
}

// 错误 7：有返回值函数缺少 return 语句 g
int test_missing_return() {
    int x = 10;
} // 错误：缺少 return（错误类型 g）

// 错误 8：修改常量值 h
void test_modify_constant() {
    const1 = 50; // 错误：不能修改常量值（错误类型 h）
}

// 错误 9：缺少分号 i
void test_missing_semicolon() {
    int x = 10 // 错误：缺少分号（错误类型 i）
}

// 错误 10：缺少右小括号 j
void test_missing_parenthesis(int x, int y {
    return x + y;
} // 错误：缺少右小括号（错误类型 j）

// 错误 11：缺少右中括号 k
void test_missing_bracket() {
    int arr[5; // 错误：缺少右中括号（错误类型 k）
}

// 错误 12：printf 格式字符与表达式个数不匹配 l
void test_printf_mismatch() {
    int x = 10;
    printf("%d %d\n", x); // 错误：printf 参数个数不匹配（错误类型 l）
}

// 错误 13：非循环块中的 break m
void test_invalid_break() {
    break; // 错误：非循环块中使用 break（错误类型 m）
}

// 正确函数，演示复杂控制流
void process_array(int arr[], int size) {
    int i;
    for (i = 0; i < size; i = i + 1) {
        if (arr[i] % 2 == 0) {
            arr[i] = arr[i] / 2;
        } else {
            arr[i] = arr[i] * 3 + 1;
        }
    }
}

// 主函数，混合触发各种错误
int main() {
    int x = 10, y = 20; // 正确
    int x = 30;         // 错误：名字重定义（错误类型 b）

    int i;
    for (i = 0; i < 10; ) { // 正确：for 缺少更新部分
        if (i == 5) {
            continue; // 正确：循环中合法使用 continue
        }
    }

    // 错误：在非循环块中使用 continue（错误类型 m）
    continue;

    {
        const int nested_const = 100;
        nested_const = 200; // 错误：修改常量值（错误类型 h）
        {
            int aaa = bbb;
            {
                int bbb = 1;
                {
                    int ccc;
                }
            }
        }
    }

    int arr[10];
    printf("%d %d", arr[5]); // 错误：参数个数不匹配（错误类型 l）

    return; // 错误：main 函数必须返回常量 0（错误类型 g）
}
