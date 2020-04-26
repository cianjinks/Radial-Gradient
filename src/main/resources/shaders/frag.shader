#version 450 core

in vec4 v_Position;
in vec4 v_Color;
in mat4 v_MVP;

float circleShape(vec2 position, float radius) {
    return step(radius, length(position - vec2(720.0f/2.0)));
}

void main(void) {
    float radius = 160.0;
    vec2 p = (v_Position.xy - vec2(720.0f/2.0f)) / radius;
    float r = sqrt(dot(p, p));
    float expand = 0.75f;
    float mixValue = (r - expand) / (1 - expand);

    vec3 color1 = v_Color.xyz;
    vec3 color2 = vec3(0.0f);
    vec3 color = mix(color1, color2, mixValue);
    gl_FragColor = vec4(color, mixValue);
}