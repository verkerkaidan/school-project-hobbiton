pub struct Exercise {
    pub exercise_id: Option<String>,
    pub name: Option<String>,
    pub gif_url: Option<String>,
    pub target_muscles: Option<Vec<String>>,
    pub body_parts: Option<Vec<String>>,
    pub equipments: Option<Vec<String>>,
    pub secondary_muscles: Option<Vec<String>>,
    pub instructions: Option<Vec<String>>,
}