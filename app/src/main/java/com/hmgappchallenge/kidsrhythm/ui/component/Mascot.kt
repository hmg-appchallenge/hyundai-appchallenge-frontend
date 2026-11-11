package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R

enum class MascotPose { Standing, Sitting }

/**
 * 마스코트 캐릭터. [label]은 동물 이름, [nickname]은 친구 고르기(S01-1)에 쓰는 이름이다.
 * 이미지는 여백 없이 잘려 있어서, 화면에서는 높이와 바닥선을 맞춰 그린다.
 */
enum class MascotCharacter(
    val label: String,
    val nickname: String,
    @DrawableRes val standingRes: Int,
    @DrawableRes val sittingRes: Int,
) {
    DOG("강아지", "붕붕이", R.drawable.img_mascot_dog_standing, R.drawable.img_mascot_dog_sitting),
    CAT("고양이", "삐뽀", R.drawable.img_mascot_cat_standing, R.drawable.img_mascot_cat_sitting),
    FROG("개구리", "두두", R.drawable.img_mascot_frog_standing, R.drawable.img_mascot_frog_sitting),
    PIG("돼지", "빵빵이", R.drawable.img_mascot_pig_standing, R.drawable.img_mascot_pig_sitting),
    LION("사자", "부릉이", R.drawable.img_mascot_lion_standing, R.drawable.img_mascot_lion_sitting),
    ;

    @DrawableRes
    fun imageRes(pose: MascotPose): Int = when (pose) {
        MascotPose.Standing -> standingRes
        MascotPose.Sitting -> sittingRes
    }
}

/** 이미지는 여백 없이 잘려 있다. 너비나 높이 하나만 정하면 나머지는 이미지 비율을 따른다. */
@Composable
fun Mascot(
    character: MascotCharacter,
    pose: MascotPose,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(character.imageRes(pose)),
        contentDescription = character.label,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview
@Composable
private fun MascotPreview() {
    Mascot(MascotCharacter.LION, MascotPose.Sitting, Modifier.width(300.dp))
}
