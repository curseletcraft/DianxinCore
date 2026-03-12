package com.dianxin.core.jdaexpansion.lavaplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trạm trộn âm thanh (Audio Mixer) cho phép phát nhiều luồng AudioPlayer cùng một lúc.
 * Hệ thống tự động trộn (mix) các byte âm thanh lại với nhau trước khi gửi cho Discord.
 */
@SuppressWarnings("unused")
public class AudioMixerSendHandler implements AudioSendHandler {

    // Danh sách các luồng phát đang hoạt động (Ví dụ: "MUSIC" -> MusicPlayer, "TTS" -> TTSPlayer)
    private final Map<String, AudioPlayer> players = new ConcurrentHashMap<>();

    // Khung âm thanh cuối cùng đã được trộn
    private byte[] lastMixedFrame;

    /**
     * Đăng ký một AudioPlayer vào một kênh cụ thể.
     * @param channelId Tên kênh (VD: "MUSIC", "TTS")
     * @param player Lavaplayer AudioPlayer
     */
    public void registerChannel(String channelId, AudioPlayer player) {
        players.put(channelId, player);
    }

    public void removeChannel(String channelId) {
        players.remove(channelId);
    }

    @Override
    public boolean canProvide() {
        // Biến lưu trữ tổng các sóng âm thanh trước khi ép kiểu về byte
        // Khung chuẩn của Opus là 3840 bytes (1920 samples * 2 kênh)
        int[] mixedAudio = new int[1920 * 2];
        boolean hasAudio = false;

        for (AudioPlayer player : players.values()) {
            AudioFrame frame = player.provide();
            if (frame != null) {
                hasAudio = true;
                mixAudio(mixedAudio, frame.getData());
            }
        }

        if (hasAudio) {
            this.lastMixedFrame = convertToBytes(mixedAudio);
            return true;
        }

        return false;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastMixedFrame);
    }

    @Override
    public boolean isOpus() {
        return false; // BẮT BUỘC PHẢI TRẢ VỀ FALSE. Khi mix âm thanh, ta đang làm việc với tín hiệu PCM thô (chưa nén).
    }

    /**
     * Thuật toán trộn tín hiệu âm thanh (PCM 16-bit Big-Endian).
     */
    private void mixAudio(int[] mixedAudio, byte[] frameData) {
        for (int i = 0; i < frameData.length; i += 2) {
            // Chuyển 2 byte thành 1 số short (16-bit PCM)
            short sample = (short) ((frameData[i] << 8) | (frameData[i + 1] & 0xFF));

            // Cộng dồn sóng âm
            mixedAudio[i / 2] += sample;
        }
    }

    /**
     * Chuyển mảng số int đã cộng dồn thành mảng byte để gửi đi.
     * Có tích hợp thuật toán Clipping (chống vỡ tiếng nếu âm lượng quá to).
     */
    private byte[] convertToBytes(int[] mixedAudio) {
        byte[] result = new byte[mixedAudio.length * 2];
        for (int i = 0; i < mixedAudio.length; i++) {
            int sample = mixedAudio[i];

            // Clipping: Giới hạn biên độ âm thanh trong khoảng của 16-bit short (-32768 đến 32767)
            if (sample > 32767) sample = 32767;
            else if (sample < -32768) sample = -32768;

            short shortSample = (short) sample;
            result[i * 2] = (byte) (shortSample >> 8);
            result[i * 2 + 1] = (byte) (shortSample & 0xFF);
        }
        return result;
    }
}
