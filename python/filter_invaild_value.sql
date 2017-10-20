create table rcgzz.zh_4g_kw_filter as
              select msisdn, domainname, k, v from rcgzz.zh_4g_kw_tmp2
                where domainname not in ('api.map.baidu.com','apis.map.qq.com','js.ub.fang.com')
                and k not in ('carrier','operator','CarrierName','carrierName','net','isp','cp','city','province','country','cityName','userCity','_city','_province','_cityNamefromStation','location','address','loc','area','_product','GetMothod')
                and v not in ('');
create table rcgzz.zh_4g_kw_filter_tmp1 as
            select v, msisdn
            from rcgzz.zh_4g_kw_filter
            group by v, msisdn;
create table rcgzz.zh_4g_kw_filter_tmp2 as
              select t0.v, t0.msisdn, sex.sex from
                  (select v, count(*) as uc from rcgzz.zh_4g_kw_filter_tmp1 group by v having count(*) >= 3) t1
                join
                  (select v,msisdn from rcgzz.zh_4g_kw_filter_tmp1) t0
                on t0.v = t1.v
                join
                  (select msisdn, sex from rcgzz.zh_sex_age) sex
                on sex.msisdn = t0.msisdn;
drop table rcgzz.zh_4g_kw_filter_tmp1;
create table rcgzz.zh_4g_kw_filter_tmp3 as
              select v, sex, count(*) as sc
              from rcgzz.zh_4g_kw_filter_tmp2
              group by v, sex;
drop table rcgzz.zh_4g_kw_filter_tmp2;
create table rcgzz.zh_4g_kw_filter_invaild as
              select * from
                  ( select man.v
                  , man.sc as man_count
                  , woman.sc as woman_count
                  , man.sc / (man.sc + woman.sc) - 0.5 as sex_rate
                  from
                    (select v, sc from rcgzz.zh_4g_kw_filter_tmp3 where sex = ' 鐢?) man
                  join
                    (select v, sc from rcgzz.zh_4g_kw_filter_tmp3 where sex = ' 濂?) woman
                  on man.v = woman.v) invaild;
              where abs(invaild.sex_rate) < 0.100000;
drop table rcgzz.zh_4g_kw_filter_tmp3;
insert overwrite table rcgzz.zh_4g_kw_filter
            select kw.msisdn, kw.domainname, kw.k, kw.v from
                (select * from rcgzz.zh_4g_kw_filter_invaild) invaild
              right outer join
                (select * from rcgzz.zh_4g_kw_filter) kw
              on invaild.v = kw.v
            where invaild.v is null;