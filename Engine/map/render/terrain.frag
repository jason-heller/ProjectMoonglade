#version 150

in vec4 pass_uvs;
in vec3 pass_normals;
in vec4 pass_colors;

uniform sampler2D grass;
uniform sampler2D bush;
uniform sampler2D snow;
uniform sampler2D sand;

uniform vec3 lightDirection;

out vec4 out_color;

const float lightDiffuse = 0.6;
const float lightScale = 1.0 - lightDiffuse;

void main(void) {
	float light = (dot(lightDirection, pass_normals)*lightScale) + lightDiffuse;
	//vec4 t1 = texture(grass, pass_uvs.xy) * distance(vec2(1,1), pass_uvs.zw);
	//vec4 t2 = texture(bush, pass_uvs.xy)  * distance(vec2(1,0), pass_uvs.zw);
	//vec4 t3 = texture(snow, pass_uvs.xy)  * distance(vec2(0,1), pass_uvs.zw);
	//vec4 t4 = texture(sand, pass_uvs.xy)  * distance(vec2(0,0), pass_uvs.zw);
	//out_color = (t1+t2+t3+t4) * pass_colors * light;
	vec4 t1 = mix(texture(grass, pass_uvs.xy), texture(sand, pass_uvs.xy), pass_uvs.z);
	vec4 t2 = mix(texture(bush, pass_uvs.xy), texture(snow, pass_uvs.xy), pass_uvs.w);
	out_color = (t1+t2)/2.0 * pass_colors * light;
	
}
