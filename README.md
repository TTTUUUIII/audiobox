# AudioBox

## 1. 关于

AudioBox是一个专为Android平台编写的音频库，支持播放和录音，目前包含以下组件：

> **播放**
> > AudioPlayer： 用于播放磁盘文件
> >
> > RawPlayer： 用于播放裸数据
>
> **录音**
>
> > AudioRecorder
>

## 2. 快速开始

### 2.1 播放

**AudioPlayer**

```java
AudioPlayer player = new AudioPlayer();
player.setAudioSource("path/to/audio.pcm");
player.play();
```

**RawPlayer**

```java
RawPlayer player = new RawPlayer();
player.setAudioSource(new RawPacket.Builder()
        .setBody(new byte[]{0, 0, 0})
        .build());
player.play();
```

### 2.2 录音

**v1.0.\***

```java
AudioRecorder<Short, short[]> recorder = new AudioRecorder<>(Short.class);

recorder.setCaptureListener(new CaptureListener<short[]>() {
    @Override
    public void onCapture(short[] data) {
        /*handle audio data*/
    }
});

recorder.start();
```
**v1.1.\***

```java
AudioRecorder<short[]> recorder = new AudioRecorder<>();

recorder.setCaptureListener(new CaptureListener<short[]>() {
    @Override
    public void onCapture(short[] data) {
        /*handle audio data*/
    }
}, short[].class);

recorder.start();
```

***
> 注：录音需要`android.permission.RECORD_AUDIO`权限。
***