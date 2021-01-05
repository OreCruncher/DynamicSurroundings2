//
// Created by Mattenii
// https://www.shadertoy.com/view/MsjfRG
// Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
//
// Shader was modified from the original to split it into separate
// vertex and frag shader scripts and adapted to fit the shader
// framework.
//
#version 120

// Time, generally in seconds.  Increasing/decreasing the value over
// time alters the color shift speed.
uniform float time;

// Size of the render area (the quad).
uniform vec2 resolution;

// Colors to use when generating the bands
uniform vec4 topColor;
uniform vec4 middleColor;
uniform vec4 bottomColor;

// Alpha to use when generating colors.
uniform float alpha;

vec2 hashConst = vec2(12.9898, 78.233);

// Weights to give the color components when calculating luminosity.  If tweaking the sum of
// the factors has to equal 1.
//vec3 LUMA_FACTORS = vec3(0.2126, 0.7152, 0.0722);
vec3 LUMA_FACTORS = vec3(0.2841, 0.5722, 0.1437);
float LUMA_THRESHOLD = 0.25F;

// Noise functions
float hash(vec2 co) {
	return fract(sin(dot(co, hashConst)) * 43758.5453);
}

float hash(float x, float y) {
	return hash(vec2(x, y));
}

float shash(vec2 co) {
	float x = co.x;
	float y = co.y;

	float corners = (hash(x - 1.0, y - 1.0) + hash(x + 1.0, y - 1.0)
			+ hash(x - 1.0, y + 1.0) + hash(x + 1.0, y + 1.0)) / 16.0;
	float sides = (hash(x - 1.0, y) + hash(x + 1.0, y) + hash(x, y - 1.0)
			+ hash(x, y + 1.0)) / 8.0;
	float center = hash(co) / 4.0;

	return corners + sides + center;
}

float noise(vec2 co) {
	vec2 pos = floor(co);
	vec2 fpos = co - pos;

	fpos = (3.0 - 2.0 * fpos) * fpos * fpos;

	float c1 = shash(pos);
	float c2 = shash(pos + vec2(0.0, 1.0));
	float c3 = shash(pos + vec2(1.0, 0.0));
	float c4 = shash(pos + vec2(1.0, 1.0));

	float s1 = mix(c1, c3, fpos.x);
	float s2 = mix(c2, c4, fpos.x);

	return mix(s1, s2, fpos.y);
}

float pnoise1(vec2 co)
{
	return noise(co);
}

float pnoise2(vec2 co)
{
	float total = 0.0;
	total = noise(co);
	total += noise(2.0 * co) * 0.5;
	return total / 1.5;
}

float pnoise3(vec2 co)
{
	float total = 0.0;
	total = noise(co);
	total += noise(2.0 * co) * 0.5;
	total += noise(4.0 * co) * 0.25;
	return total / 1.75;
}

float pnoise4(vec2 co)
{
	float total = 0.0;
	total = noise(co);
	total += noise(2.0 * co) * 0.5;
	total += noise(4.0 * co) * 0.25;
	total += noise(8.0 * co) * 0.125;
	return total / 1.875;
}

// FBM: repeatedly apply Perlin noise to position
vec2 fbm1(vec2 p)
{
	return vec2(pnoise1(p + vec2(time, 0.0)),
			pnoise1(p + vec2(-time, 0.0)));
}

vec2 fbm2(vec2 p)
{
	return vec2(pnoise2(p + vec2(time, 0.0)),
			pnoise2(p + vec2(-time, 0.0)));
}

vec2 fbm3(vec2 p)
{
	return vec2(pnoise3(p + vec2(time, 0.0)),
			pnoise3(p + vec2(-time, 0.0)));
}

vec2 fbm4(vec2 p)
{
	return vec2(pnoise4(p + vec2(time, 0.0)),
			pnoise4(p + vec2(-time, 0.0)));
}

float fbm2_1(vec2 p) {
	return pnoise1(p + 10.0 * fbm1(p) + vec2(0.0, time));
}

float fbm2_2(vec2 p) {
	return pnoise2(p + 10.0 * fbm2(p) + vec2(0.0, time));
}

float fbm2_3(vec2 p) {
	return pnoise3(p + 10.0 * fbm3(p) + vec2(0.0, time));
}

float fbm2_4(vec2 p) {
	return pnoise4(p + 10.0 * fbm4(p) + vec2(0.0, time));
}

// Calculate the lights themselves
vec3 lights(vec2 co) {
	float d, r, g, b, h;
	vec3 rc, gc, bc, hc;

	// Red (top)
	r = fbm2_1(co * vec2(1.0, 0.5));
	d = pnoise1(2.0 * co + vec2(0.3 * time));
	rc = topColor.xyz * r * smoothstep(0.0, 2.5 + d * r, co.y)
			* smoothstep(-5.0, 1.0, 5.0 - co.y - 2.0 * d);

	// Green (middle)
	g = fbm2_4(co * vec2(2.0, 0.5));
	gc = 0.8 * middleColor.xyz
			* clamp(2.0 * pow((3.0 - 2.0 * g) * g * g, 2.5) - 0.5 * co.y, 0.0,
					1.0) * smoothstep(-2.0 * d, 0.0, co.y)
			* smoothstep(0.0, 0.3, 1.1 + d - co.y);

	g = fbm2_2(co * vec2(1.0, 0.2));
	gc += 0.5 * middleColor.xyz
			* clamp(2.0 * pow((3.0 - 2.0 * g) * g * g, 2.5) - 0.5 * co.y, 0.0,
					1.0) * smoothstep(-2.0 * d, 0.0, co.y)
			* smoothstep(0.0, 0.3, 1.1 + d - co.y);

	// Blue (bottom)
	h = pnoise1(vec2(5.0 * co.x, 5.0 * time));
	hc = bottomColor.xyz * pow(h + 0.1, 2.0)
			* smoothstep(-2.0 * d, 0.0, co.y + 0.2)
			* smoothstep(-h, 0.0, -co.y - 0.4);

	return rc + gc + hc;
}

void main() {

	vec2 uv = gl_TexCoord[0].st;
	vec2 co = (uv * resolution) / resolution.y;
	vec3 col = vec3(0.0);

	// Aurora (with some transformation)
	float f = 0.15 * pnoise1(vec2(5.0 * uv.x, 0.3 * time));
	vec2 aco = co;
	aco.y -= f;// This affects the distance from the bottom of the panel
	// aco *= 10.0 * uv.x + 5.0;
	aco *= 10.0 * uv.x + 20.0;
	col += 0.5 * lights(aco)
	* (smoothstep(0.3, 0.6, pnoise1(vec2(10.0 * uv.x, 0.3 * time)))
	+ 0.5
	* smoothstep(0.5, 0.7, pnoise1(vec2(10.0 * uv.x, time))));

	// May need to tweak the alpha
	float adjustAlpha = alpha;

	// Calculate the luminosity of the generated color.  If it is low scale the alpha downward and increase color
	// luminosity.  This should minimize generation of dark/black colors in favor of fading.  Looks better when
	// mixing the texture into the scene.
	float lumaCalc = dot(col, LUMA_FACTORS);
	if (lumaCalc < LUMA_THRESHOLD) {
		adjustAlpha *= lumaCalc / LUMA_THRESHOLD;

		// Brighten the color by scaling up using luma
		float t = LUMA_THRESHOLD - lumaCalc;
		col *= (1 + LUMA_FACTORS * t);
	}

	// Done!
	gl_FragColor = vec4(col, adjustAlpha);
}
