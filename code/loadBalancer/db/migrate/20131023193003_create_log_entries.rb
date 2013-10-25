class CreateLogEntries < ActiveRecord::Migration
  def change
    create_table :log_entries do |t|
      t.integer :'session_id'
      t.string :'msg'
    end
  end
end
