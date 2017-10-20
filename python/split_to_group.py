#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python split_to_group.py file_path split_count
#**功能描述：将目录里的文件按数量分组
#**输入参数：路径 数量
#**输出参数：无
#**返回值：无
#**创建者：陈东成
#**创建日期：20170925
#**修改日志：
#**
#**************************************************************************
##导入配置信息

import sys
import os
import glob
import shutil

if __name__ == '__main__':

    #获取参数
    file_path = sys.argv[1]
    print '路径：' + file_path

    split_count = 1000
    if len(sys.argv) > 2:
        split_count = int(sys.argv[2])
    print '数量：' + str(split_count)

    file_list = glob.glob(os.path.join(file_path, '*'))
    fIndex = 0
    gIndex = 0
    group_path = ''

    for file in file_list:

        #分组
        if fIndex % split_count == 0:
            gIndex += 1
            group_path = os.path.join(file_path, 'group_' + str(gIndex))
            print group_path
            os.mkdir(group_path)

        fIndex += 1

        #将文件移进文件夹
        file_name = os.path.basename(file)
        shutil.move(file, os.path.join(group_path, file_name))

    print 'OK'
