#!/bin/sh

CWD=`dirname $_`
BREAK_CHARS="(){}[],^%$#@\"\";:''|\\"
CLOJURE_CP="`find $CWD/libs -name *.jar -print0 | tr \\\\0 :`$CWD/src"

if [ $# -eq 0 ]; then
  RLWRAP="rlwrap --remember -c -b $BREAK_CHARS -f $CWD/.clj_completions"
fi

$RLWRAP java $CLOJURE_PARAMS -cp $CLOJURE_CP clojure.main "$@"
