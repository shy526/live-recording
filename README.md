# live-recording

- 生成直播流文件


## bug
- [ ] 播放时间信息错误

- [x] 个别直播流关闭时阻塞的问题


## 待开发
- [x] m3u8格式的直播推流方式recording实(只测试了猫耳)

- [ ] ~~一图流flv生成,应对大容量音频无法上传的问题~~

- [ ] ~~询问请求使用代理~~(免费的代理效果太差)

- [x] 简易代理池

## 暂时支持的直播流

- [x] bilibili

- [x] 猫耳


## 接口

{host}/live/stop/{source}/{roomId}

{host}/live/recording/{source}

{host}/live/sources
