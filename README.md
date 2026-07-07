# Round Ontology Gen-Val Loop

这个项目的设计收敛为一个最小闭环：

```text
TaskSpec
  -> RoundInput
  -> GenAgent
  -> Candidate
  -> ValAgent
  -> ValidationResult
  -> ConvergenceRule
  -> RoundOutput
  -> next RoundInput if not converged
```

这里的 ontology 不再被建模成一组复杂空间和中介层。它只做一件事：

```text
用 ontology statements 表达每一轮的 input / process / output。
```

## Round-Level Ontology

每一轮都有三个 frame：

```text
RoundInput.ontology
  本轮生成之前，系统知道什么、目标是什么、上一轮反馈是什么。

RoundProcess.ontology
  本轮执行了哪些过程：GenAgent 生成、ValAgent 验证、ConvergenceRule 比较。

RoundOutput.ontology
  本轮产出了什么 candidate、validation、feedback、decision。
```

最小 ontology statement：

```java
record OntologyStatement(
    StatementKind kind,
    String subject,
    String predicate,
    String object
) {}
```

`StatementKind` 包括：

```text
FACT
CONCEPT
INTENTION
GOAL
CRITERION
CANDIDATE
VALIDATION
FEEDBACK
DECISION
EVIDENCE
```

这个设计的关键点是：ontology 是每一轮的可追踪表达，而不是先验地把系统拆成过重的理论层。

## Test Case

全链路测试入口：

```text
src/test/java/com/ontogenval/loop/RoundOntologyLoopTest.java
```

测试场景：

```text
round 1:
  input ontology 包含 task facts / concepts / goals / criteria
  GenAgent 生成 candidate
  ValAgent 判断 candidate 缺少 DONE / evidence
  output ontology 包含 validation 和 feedback
  loop 不收敛

round 2:
  input ontology 携带上一轮 feedback
  GenAgent 根据 feedback 修正 candidate
  ValAgent 判断 valid=true
  ConvergenceRule 判定收敛
  output ontology 包含 final decision
```

它验证的是：

```text
1. 每轮 input/process/output 都有 ontology 表达。
2. 不收敛时，feedback 进入下一轮 input ontology。
3. 收敛不是凭空发生，而是由 ValidationResult + ConvergenceRule 给出。
```

## Run

如果本机有 Java 17+ 和 Maven：

```powershell
mvn test-compile
java -cp "target/classes;target/test-classes" com.ontogenval.loop.RoundOntologyLoopTest
```

示例程序：

```powershell
mvn compile
java -cp "target/classes" com.ontogenval.example.MinimalRoundOntologyApp
```

当前工作区环境没有 `java`、`javac`、`mvn`，所以这里无法实际编译运行。代码按 Java 17/Maven 标准结构生成。

## Mock Task JSONL

测试用 mock task 数据：

```text
src/test/resources/mock-tasks/aerospace_adcs_tasks.jsonl
```

每行对应一个 `TaskSpec`：

```text
taskId, objective, target, facts, concepts, goals, criteria
```

数据覆盖航空制造和卫星姿轨控相关场景，但只用于软件测试。样例刻意避免真实工艺参数、飞行放行判断、控制律增益、航天器指令、变轨时序和 delta-v 等操作性内容。

说明见：

```text
docs/mock-task-jsonl.md
```

## Database Schema

数据库 schema 已生成：

```text
db/migrations/V001__round_ontology_schema.sql
```

它围绕 round trace 建模：

```text
task
  -> loop_run
  -> round_trace
  -> ontology_frame(INPUT|PROCESS|OUTPUT)
  -> ontology_statement
  -> candidate
  -> validation_result
```

说明见：

```text
docs/database-schema.md
```

## Google ADK Position

Google ADK 可以作为后续 runtime adapter，但不应该改变这个核心规范。

推荐边界：

```text
Core loop:
  RoundInput / RoundProcess / RoundOutput / OntologyFrame

Runtime adapter:
  Google ADK agent or model runner implements ModelClient
```

也就是说，ADK 承载执行，ontology frame 承载每轮语义记录。
