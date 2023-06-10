# Compilers Project (Group 7B)

## 👥 Group members 
- Isabel Amaral   (up202006677)
- Mariana Rocha   (up202004656)
- Milena Gouveia  (up202008862)

## 👩‍💻 Distribution of work
- Isabel Amaral   (33%)   - Jasmin
- Mariana Rocha   (33%)   - Ollir
- Milena Gouveia  (33%)   - Semantic Analysis and Optimizations

## 🪛 Implemented optimizations
- Option `-o`:
  - constant propagation
  - constant folding
- Option `–r=<n>` (register allocation):
  - `n ≥ 1`: the compiler tries to use at most `<n>` local variables when generating Jasmin instructions. It aborts and reports an error if `<n>` is not enough to store the local variables.
  - `n = −1`: This is the default value where the compiler uses as many variables as originally present in the OLLIR representation.

## 🔎 Self-assessment
The developed project seems to be working as expected except for the optimization where the compiler tries to use the fewest registers as possible (option `-r 0`). However, considering that all the project requirements and all the remaining optimizations were implemented, we believe that our project deserves a grade of 19-19.5 out of 20.
