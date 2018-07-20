* Added support for defines. Use `#define A B` to create or replace a define, use `#undef A` to remove a define. Defined names can be used where targets or number literals are expected.
* Added two new instructions, `RRLAST` and `RLLAST`, used to rotate the value of `LAST` one to the right or the left, respectively, if its value is not `NIL`.
* Fix crash when controllers in errored state are in view and player is looking at an Entity (as opposed to a block).
* Fix crash when rendering module overlay when player is looking at an Entity (as opposed to a block).
