export GatorRoot=$(dirname "$(readlink -f "$0")")
echo "GatorRoot: $GatorRoot"

export ANDROID_SDK=$1
echo "ANDROID_SDK: $ANDROID_SDK"

cd $GatorRoot/gator
./gator b