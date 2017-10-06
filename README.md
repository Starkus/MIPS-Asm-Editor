# MIPS-Asm-Editor
MIPS Assembly code editor with syntax highlighting and tooltips.

The main goal of the app is to make it a little easier to learn Assembly programming.
The syntax is based off of Caje-ASM assembler mainly but the idea is to make it work for most mainstream assemblers.

![screenshot](https://i.imgur.com/2HbVLoW.png)

Current features:
* Code highlighting.
* File tabs.
* Tooltips on opcode and register names with info like syntax and a little description.
* Tooltips on hex literals which tell the decimal and float values.
* WIP code interpretation, right now only defines work.
* .include directives works with code interpretation.
* .org directive as defined for Caje-ASM.
* Auto-completion for opcodes and register names.
* Auto-completion of defined constants with their documentation.

Planned features:

* Real time compiling.
* Checking for syntax errors.
* Show where a selected Jump or Branch jumps to.
* Maybe turn it into a project-based IDE.
