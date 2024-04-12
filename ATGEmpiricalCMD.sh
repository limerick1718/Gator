echo "Gator"

apk_path=$1
sdk_path=$2
result_dir=$3
working_dir=$(pwd)

apkname=$(basename $apk_path)

apk_path=$(realpath $apk_path)
sdk_path=$(realpath $sdk_path)

echo "APK path: $apk_path"
echo "Android Sdk path: $sdk_path"
echo "Apk Name: $apkname"
echo "Working dir: $working_dir"

export ANDROID_SDK=$sdk_path

if [ ! -d "$result_dir" ]; then
	mkdir -p "$result_dir"
fi
result_dir=$(realpath $result_dir)
echo "Result dir: $result_dir"

gator_dir=$(dirname "$(readlink -f "$0")")"/gator"
echo "Gator dir: $gator_dir"
cd $gator_dir

START=$(date +%s)
./gator a -p $apk_path -client WTGDemoClient > $result_dir/gator.log
END=$(date +%s)
DIFF=$(( $END - $START ))
echo "Processed in $DIFF seconds."  >> $result_dir/gator.log
