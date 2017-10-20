#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python csv_to_file.py kw_csv_file output_path min_url_count
#**功能描述：kw的csv数据按user合并，并且输出为单独文件夹
#**输入参数：kw_csv_file 为csv文件，要求列分别为msisdn,sex,age,kw，并按msisdn进行排序
#**        output_path 文件夹路径，默认为output文件夹（要求文件夹存在）
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
import pandas as pd

if __name__ == '__main__':

  reload(sys)
  sys.setdefaultencoding('utf-8')

  #获取参数
  kw_csv_file = sys.argv[1]
  print 'CSV文件：' + kw_csv_file

  output_path = kw_csv_file[:-4]
  if len(sys.argv) > 2:
    output_path = sys.argv[2]
  print '输出路径：' + output_path

  #读取CSV文件
  csv = pd.read_csv(kw_csv_file, sep='\t', header= None, encoding= 'utf-8')
  lines = csv.values

  #判断输出路径是否存在，不存在则建立
  if not os.path.exists(output_path):
    os.mkdir(output_path)

  msisdn = ''
  sex = ''
  age = ''
  file = None
  texts = []
  lIndex = 0
  skipCount = 0

  for line in lines:
    #计数
    lIndex += 1
    if skipCount > int(len(lines) * 0.01):
      print '错误过多: ' + str(skipCount)
      exit(1)

    #校验格式
    if len(line) != 4:
      print 'index:' + str(lIndex) + ', len:' + str(len(line)), line
      skipCount += 1
      continue

    if line[1] not in ('男', '女'):
      print 'index:' + str(lIndex) + ', sex:' + line[1]
      skipCount += 1
      continue

    try:
      line[2] = str(int(line[2]))
    except:
      print 'index:' + str(lIndex) + ', age:' + line[2]
      skipCount += 1
      continue

    # 如果msisdn发生改变
    if msisdn != line[0]:
      # 保存旧用户数据
      if len(texts) > 0:
        filename = sex + '-' + age + '-' + msisdn + '.txt'
        file = open(os.path.join(output_path, filename), 'w') # 建立新用户文件
        file.write('\n'.join(texts)) # 将访问内容写入文件
        file.close() #关闭文件
      # 新用户
      msisdn = line[0]
      sex = line[1]
      age = line[2]
      texts = []

    # 判断是否重复
    if len(texts) > 1:
      try:
        if texts[-1] in line[3]:
          texts[-1] = line[3]
          continue
      except:
        print 'index:' + str(lIndex) + ', prev_text:' + texts[-1] + ', curr_text: ' + line[3]
        skipCount += 1
        continue

    texts.append(line[3])


  # 执行最后一次判断
  if len(texts) > 0 : # 仅保留多于N条记录的用户
    filename = sex + '-' + age + '-' + msisdn + '.txt'
    file = open(os.path.join(output_path, filename), 'w')  # 建立新用户文件
    file.write('\n'.join(texts))  # 将访问内容写入文件
    file.close()  # 关闭文件

  print 'OK'

