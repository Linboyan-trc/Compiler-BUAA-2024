# Compiler
### Repository
- 此仓库为BUAA-2024 Compiler课程

### 实验通过情况
- 文法阅读 ✅
- 词法分析 ✅
- 语法分析 ✅
- 语义分析 ✅
- 代码生成一 ✅
- 代码生成二 ✅

### Compiler Structure
- Frontend
  - 完成词法分析、语法分析
  - 构建抽象语法树
- Midend
  - 将抽象语法树转换成中间代码
- Backend
  - 将中间代码转换为目标代码`MIPS`

### Input Files
- `testfile.c`: 需要编译的`C Language`源代码(`SysY`)
- `testfile.txt`: 需要编译的`C Language`源代码(`SysY`)的.`txt`格式

### Output Files
- `lexer.txt`: 词法分析结果
- `parser.txt`: 语法分析结果
- `symbol.txt`: 符号表
- `error.txt`: 错误处理
- `temp.txt`: 临时错误处理
- `midcode.txt`: 中间代码
- `mips.txt`: 目标代码`MIPS`
