package com.abedi.arnaja;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private ExternalTexture texture;
    private CustomArFragment arFragment;
    private ModelRenderable renderable;
    private MediaPlayer mediaPlayer;
    private Scene scene;
    private boolean isImageDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this, R.raw.video);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        ModelRenderable.builder()
                .setSource(this, Uri.parse("raw/video.mp4"))
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.getMaterial()
                            .setExternalTexture("videoTexture", texture);
                    modelRenderable.getMaterial().setFloat4("keyColor", new Color(
                            0.01843f, 1f, 0.098f));
                    renderable = modelRenderable;
                });
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        scene = arFragment.getArSceneView().getScene();
        scene.addOnUpdateListener(this::onUpdate);

    }

    private void onUpdate(FrameTime frameTime) {

        if (isImageDetected)
            return;

        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage image :
                augmentedImages) {
            if (image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("image")) {
                    isImageDetected = true;
                    playVide(image.createAnchor(image.getCenterPose()), image.getExtentX(), image.getExtentZ());
                    break;
                }
            }

        }
    }

    private void playVide(Anchor anchor, float extentX, float extentZ) {

        mediaPlayer.start();
        AnchorNode node = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            node.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);

        });

        node.setWorldScale(new Vector3(extentX, 1f, extentZ));
        scene.addChild(node);
    }
}




